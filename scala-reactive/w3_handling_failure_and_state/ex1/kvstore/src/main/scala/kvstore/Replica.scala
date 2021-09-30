package kvstore

import akka.actor.{ OneForOneStrategy, PoisonPill, Props, SupervisorStrategy, Terminated, ActorRef, Actor, actorRef2Scala }
import kvstore.Arbiter.*
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration.*
import akka.util.Timeout
import java.lang.Math.max

object Replica:
  sealed trait Operation:
    def key: String
    def id: Long
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  case class OperationTimeout(id: Long)

  case class ResendStore(key: String)

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(Replica(arbiter, persistenceProps))

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor:
  import Replica.*
  import Replicator.*
  import Persistence.*
  import context.dispatcher
  
  var kv = Map.empty[String, String]

  override def preStart(): Unit = {
    super.preStart()
    arbiter ! Join
  }

  def receive =
    case JoinedPrimary   => {
      val replicator = context.actorOf(Replicator.props(self))
      secondaries += (self, replicator)
      replicators += replicator

      context.become(leader)
    }
    case JoinedSecondary => context.become(replica)




  // Behavior for the LEADER role.
  var secondaries = Map.empty[ActorRef, ActorRef] // (Replica -> Replicator)
  var replicators = Set.empty[ActorRef]
  var inFlightReplications = Map.empty[Long, (ActorRef, Set[ActorRef])] // (Operation.id -> (operation sender, Set(Replicator)))

  val leader: Receive = {
    case Insert(key, value, id) => {
      System.out.println(s"leader received: Insert${(key, value, id)}")
      kv += (key -> value)
      replicators.foreach(_ ! Replicate(key, Some(value), id))
      inFlightReplications += (id, (sender(), replicators))
      context.system.scheduler.scheduleOnce(1000.milliseconds, self, OperationTimeout(id))
    }

    case Remove(key, id) => {
      System.out.println(s"leader received: Remove${(key, id)}")
      kv -= key
      replicators.foreach(_ ! Replicate(key, None, id))
      inFlightReplications += (id, (sender(), replicators))
      context.system.scheduler.scheduleOnce(1000.milliseconds, self, OperationTimeout(id))
    }

    case Replicated(key, id) if inFlightReplications.contains(id) => {
      System.out.println(s"leader received: Replicated${(key, id)} from ${sender()}")
      decrementInFlightReplication(id, sender())
    }
    case Replicated(key, id) if id != -1 &&  !inFlightReplications.contains(id) => {
      System.out.println(s"leader received: Replicated${(key, id)} from ${sender()} but TOO LATE")
    }
    case Replicated(key, id) if id == -1 => {
      System.out.println(s"leader received: Replicated${(key, id)} from ${sender()} new replica")
    }

    case OperationTimeout(id)  => {
      if (inFlightReplications.contains(id)) {
        inFlightReplications(id)._1 ! OperationFailed(id)
        inFlightReplications -= id
      }
    }

    case Replicas(newReplicas: Set[ActorRef]) => {
      System.out.println(s"Received replicas: $newReplicas")
      //Update tracking structures for join and leave
      val addedReplicas = newReplicas -- secondaries.keySet
      val newSecondaries = addedReplicas.map(newReplica => (newReplica, context.actorOf(Replicator.props(newReplica)))).toMap
      secondaries ++= newSecondaries
      replicators ++= newSecondaries.values.toSet
      newSecondaries.foreach( (_, replicator) => {
        kv.foreach((key, value) => replicator ! Replicate(key, Some(value), -1))
      })

      val removedSecondaries = secondaries -- newReplicas
      secondaries --= removedSecondaries.keySet
      replicators --= removedSecondaries.values.toSet
      removedSecondaries.foreach( (_, replicator) => {
        context.stop(replicator)
        inFlightReplications.foreach((id, _) => decrementInFlightReplication(id, replicator))
      })
    }

    case message => replica(message) // A leader also behaves as a replica for persistence and get
  }

  def decrementInFlightReplication(id: Long, doneReplicator: ActorRef): Unit = {
    val inFlightReplicationUpdate = (inFlightReplications(id)._1, inFlightReplications(id)._2 - doneReplicator)
    if (inFlightReplicationUpdate._2.isEmpty) {
      inFlightReplicationUpdate._1 ! OperationAck(id)
      inFlightReplications -= id
    } else {
      inFlightReplications += (id, inFlightReplicationUpdate)
    }
  }




  // Behavior for the REPLICA role.
  val persistence = context.actorOf(persistenceProps) // supervisorStrategy defaults to always restart failing child
  var inFlightPersist = Map.empty[String, (ActorRef, Persist)]
  var expectedSeq = 0L

  val replica: Receive = {
    case Get(key, id) => {
      System.out.println(s"received: Get${(key, id)}")
      sender ! GetResult(key, kv.get(key), id)
    }

    case message => persistenceHandler(message)
  }

  val persistenceHandler: Receive = {
    case s: Snapshot => {
      System.out.println(s"received: $s")
      if (s.seq < expectedSeq) {
        sender ! SnapshotAck(s.key, s.seq)
      } else if (s.seq == expectedSeq) {
        forwardStore(Persist(s.key, s.valueOption, s.seq))
      }
    }

    case p: Persisted => {
      val (sender, persist) = inFlightPersist(p.key)
      val seq = persist.id
      sender ! SnapshotAck(persist.key, seq)
      inFlightPersist -= p.key
      expectedSeq = max(expectedSeq, seq + 1)
    }

    case r: ResendStore => {
      inFlightPersist.get(r.key) match {
        case Some((sender, persist)) => scheduleStoreRetry(sender, persist)
        case None => //nil
      }
    }

    case unknownMessage =>
      System.out.println(s"received UNKNWON message: $unknownMessage")
  }

  def forwardStore(p: Persist): Unit = {
    scheduleStoreRetry(sender, p)
    p.valueOption match {
      case None => kv -= p.key
      case Some(value) => kv += (p.key -> value)
    }
  }

  def scheduleStoreRetry(sender: ActorRef, p: Persist): Unit = {
    persistence ! p
    inFlightPersist += (p.key -> (sender,p))
    context.system.scheduler.scheduleOnce(100.milliseconds, self, ResendStore(p.key))
  }


