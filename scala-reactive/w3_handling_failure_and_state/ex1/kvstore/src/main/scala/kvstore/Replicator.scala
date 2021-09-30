package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import scala.concurrent.duration.*

object Replicator:
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)
  
  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  case class ResendReplicates()

  def props(replica: ActorRef): Props = Props(Replicator(replica))

// One replicator per Replica
class Replicator(val replica: ActorRef) extends Actor:
  import Replicator.*
  import context.dispatcher

  var acks = Map.empty[Long, (ActorRef, Replicate)] // seq -> (sender, Replicate request)
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]
  
  var _seqCounter = 0L
  def nextSeq() =
    val ret = _seqCounter
    _seqCounter += 1
    ret

  context.system.scheduler.scheduleAtFixedRate(100.milliseconds, 100.milliseconds, self, ResendReplicates)

  def receive: Receive = {
    case r: Replicate => {
      val seq = nextSeq()
      acks = acks + (seq -> (sender, Replicate(r.key, r.valueOption, r.id)))
      val snapshot = Snapshot(r.key, r.valueOption, seq)
      replica ! snapshot
    }
    case SnapshotAck(key, seq) => {
      acks.get(seq).foreach{ case (sender, r) => sender ! Replicated(r.key, r.id)}
      acks = acks - seq
    }
    case ResendReplicates =>
      acks.foreach{ case (seq, (sender, r)) => replica ! Snapshot(r.key, r.valueOption, seq) }
    case _ =>
  }

