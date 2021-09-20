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

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */
  
  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  val persistence = context.actorOf(persistenceProps)

  override def preStart(): Unit = {
    super.preStart()
    arbiter ! Join
  }

  def receive =
    case JoinedPrimary   => context.become(leader)
    case JoinedSecondary => context.become(replica)

  /* TODO Behavior for  the leader role. */
  val leader: Receive = {
    case Insert(key, value, id) => {
      kv = kv + (key -> value)
      sender ! OperationAck(id)
    }
    case Remove(key, id) => {
      kv = kv - key
      sender ! OperationAck(id)
    }
    case Get(key, id) => {
      sender ! GetResult(key, kv.get(key), id)
    }
    case _ =>
  }

  
  var expectedSeq = 0L
  
  /* TODO Behavior for the replica role. */
  val replica: Receive = {
    case s: Snapshot => {
      if (s.seq < expectedSeq) {
        sender ! SnapshotAck(s.key, s.seq)
      } else if (s.seq == expectedSeq) {
        scheduleStore(sender, Persist(s.key, s.valueOption, s.seq))
        expectedSeq = max(expectedSeq, s.seq + 1)
        s.valueOption match {
          case None => kv = kv - s.key
          case Some(value) => kv = kv + (s.key -> value)
        }
      }
    }

    case p: Persisted => {
      val (sender, persist) = inFlightPersist(p.key)
      val seq = persist.id
      sender ! SnapshotAck(persist.key, seq)
      inFlightPersist = inFlightPersist - p.key
    }

    case ResendStore(key) => {
      inFlightPersist.get(key) match {
        case Some((sender, persist)) => scheduleStore(sender, persist)
        case None => //nil
      }
    }

    case Get(key, id) => {
      sender ! GetResult(key, kv.get(key), id)
    }

    case _ =>
  }

  var inFlightPersist = Map.empty[String, (ActorRef, Persist)]

  def scheduleStore(sender: ActorRef, p: Persist): Unit = {
    persistence ! p
    inFlightPersist = inFlightPersist + (p.key -> (sender,p))
    context.system.scheduler.scheduleOnce(100.milliseconds, self, ResendStore(p.key))
  }


