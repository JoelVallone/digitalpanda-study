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
      context.become(leader)
      secondaries += (self, context.actorOf(Replicator.props(self)))
      replicators += self
    }
    case JoinedSecondary => context.become(replica)




  // Behavior for the LEADER role.
  var secondaries = Map.empty[ActorRef, ActorRef] // (Replica -> Replicator)
  var replicators = Set.empty[ActorRef]
  var inFlightReplications = Map.empty[Long, (ActorRef, Set[ActorRef])] // (Operation.id -> (operation sender, Set(Replicator)))

  //TODO: test+fix primary persistence & replication
  val leader: Receive = {
    case Insert(key, value, id) => {
      kv += (key -> value)
      //sender ! OperationAck(id)
      // // forwardStore(Persist(key, Some(value), id))
      replicators.foreach( r =>  r.forward(Replicate(key, Some(value), id)))
      inFlightReplications += (id, (sender(), replicators))
    }
    case Remove(key, id) => {
      kv -= key
      //sender ! OperationAck(id)
      // // forwardStore(Persist(key, None, id))
      replicators.foreach( r =>  r.forward(Replicate(key, None, id)))
      inFlightReplications += (id, (sender(), replicators))
    }

    // TODO: check write ordering: last write request wins... Replicator.__seqCounter should do the trick
    case Replicated(key, id) => {
      val inFlightReplicationUpdate = (inFlightReplications(id)._1, inFlightReplications(id)._2 - sender())
      if (inFlightReplicationUpdate._2.isEmpty) {
        inFlightReplicationUpdate._1 ! OperationAck(id)
        inFlightReplications -= id
      } else {
        inFlightReplications += (id, inFlightReplicationUpdate)
      }
    }
    /*
    case Get(key, id) => {
      sender ! GetResult(key, kv.get(key), id)
    }*/
    case Replicas(newReplicas: Set[ActorRef]) => {
      //Update tracking structures for join and leave
      val addedReplicas = newReplicas -- secondaries.keySet
      val newSecondaries = addedReplicas.map(newReplica => (newReplica, context.actorOf(Replicator.props(newReplica)))).toMap
      secondaries ++= newSecondaries
      replicators ++= newSecondaries.values.toSet

      val removedSecondaries = secondaries -- newReplicas
      removedSecondaries.foreach{ (_, replicator) => context.stop(replicator)}
      secondaries --= removedSecondaries.keySet
      replicators --= removedSecondaries.values.toSet


      //TODO: Handle transmission of initial state to new replica
    }
    case message => replica(message) // A leader also behaves as a replica (persistence and get)
  }




  // Behavior for the REPLICA role.
  val persistence = context.actorOf(persistenceProps) // supervisorStrategy defaults to always restart failing child
  var inFlightPersist = Map.empty[String, (ActorRef, Persist)]
  var expectedSeq = 0L

  val replica: Receive = {
    case Get(key, id) => {
      sender ! GetResult(key, kv.get(key), id)
    }

    case message => persistenceHandler(message)
  }

  val persistenceHandler: Receive = {
    case s: Snapshot => {
      if (s.seq < expectedSeq) {
        sender ! SnapshotAck(s.key, s.seq)
      } else if (s.seq == expectedSeq) {
        expectedSeq = max(expectedSeq, s.seq + 1)
        forwardStore(Persist(s.key, s.valueOption, s.seq))
      }
    }
    case p: Persisted => {
      val (sender, persist) = inFlightPersist(p.key)
      val seq = persist.id
      sender ! SnapshotAck(persist.key, seq)
      inFlightPersist -= p.key
    }
    case r: ResendStore => {
      inFlightPersist.get(r.key) match {
        case Some((sender, persist)) => scheduleStoreRetry(sender, persist)
        case None => //nil
      }
    }
    case _ =>
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


