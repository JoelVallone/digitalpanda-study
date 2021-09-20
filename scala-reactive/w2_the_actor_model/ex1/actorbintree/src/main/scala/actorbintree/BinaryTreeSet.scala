/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor.*
import scala.collection.immutable.Queue

object BinaryTreeSet:

  trait Operation:
    def requester: ActorRef
    def id: Int
    def elem: Int

  trait OperationReply:
    def id: Int

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection */
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply



class BinaryTreeSet extends Actor with ActorLogging with Stash :
  import BinaryTreeSet.*
  import BinaryTreeNode.*

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional (used to stash incoming operations during garbage collection)
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {
    case op: Operation =>  {
      log.debug(s"Run: Operation(${op.id},${op.elem}) ")
      root ! op
    }
    case GC => {
      log.debug("Start garbage collection")
      val newRoot = createRoot
      root ! CopyTo(newRoot)
      context.become(garbageCollecting(newRoot))
    }
    case _ => // nil
  }

  // optional
  /** Handles messages while garbage collection is performed.
    * `newRoot` is the root of the new binary tree where we want to copy
    * all non-removed elements into.
    */
  def garbageCollecting(newRoot: ActorRef): Receive = {
    case op: Operation => {
      pendingQueue = pendingQueue.enqueue(op)
      log.debug(s"Queued (${pendingQueue.size}): Operation(${op.id},${op.elem}) ")
      //stash()
    }
    case CopyFinished => {
      log.debug(s"CopyFinished: run ${pendingQueue.size} queued operations")
      root = newRoot
      pendingQueue.foreach(op => root ! op)
      pendingQueue = Queue.empty[Operation]
      log.debug("Operations catchup finished")
      log.debug("Finished garbage collection")
      context.unbecome()
      //unstashAll()
    }
    case _ => // nil
  }


object BinaryTreeNode:
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  /**
   * Acknowledges that a copy has been completed. This message should be sent
   * from a node to its parent, when this node and all its children nodes have
   * finished being copied.
   */
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode],  elem, initiallyRemoved)

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor with ActorLogging :
  import BinaryTreeNode.*
  import BinaryTreeSet.*

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  def childInsert(pos: Position, insert: Insert) : Unit =
    if (subtrees.contains(pos)) {
      subtrees(pos) ! insert
    } else {
      subtrees += (pos -> context.actorOf(BinaryTreeNode.props(insert.elem, false)))
      insert.requester ! OperationFinished(insert.id)
    }

  def hasChildWhich(pos: Position, contains: Operation) : Unit =
    if (subtrees.contains(pos)) {
      subtrees(pos) ! contains
    } else {
      contains.requester ! ContainsResult(contains.id, false)
    }

  def childRemove(pos: Position, remove: Operation) : Unit =
    if (subtrees.contains(pos)) {
      subtrees(pos) ! remove
    } else {
      remove.requester ! OperationFinished(remove.id)
    }

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case op: Insert =>
      if (elem == 0) log.debug(s"Insert(${op.id},${op.elem})")
      if (elem == op.elem) {
        removed = false
        op.requester ! OperationFinished(op.id)
      } else if (op.elem < elem) {
        childInsert(Left, op)
      } else {
        childInsert(Right, op)
      }
    case op: Contains =>
      if (elem == 0) log.debug(s"Contains(${op.id},${op.elem})")
      if (elem == op.elem) {
        op.requester ! ContainsResult(op.id, !removed)
      } else if (op.elem < elem) {
        hasChildWhich(Left, op)
      } else {
        hasChildWhich(Right, op)
      }
    case op: Remove =>
      if (elem == 0) log.debug(s"Remove(${op.id},${op.elem})")
      if (elem == op.elem) {
        removed = true
        op.requester ! OperationFinished(op.id)
      } else if (op.elem < elem) {
        childRemove(Left, op)
      } else {
        childRemove(Right, op)
      }
    case CopyTo(treeNode) => {
      log.debug(s"BinaryTreeNode($elem,$removed)!CopyTo($treeNode)")
      if (!removed) {
        treeNode ! Insert(self, 0, elem)
      }
      subtrees.foreach{ case (_, subtree) => subtree ! CopyTo(treeNode)}

      if (removed && subtrees.isEmpty) { // corner case when only the empty root exists
        sender ! CopyFinished
      } else {
        context.become(copying(subtrees.values.toSet, insertConfirmed=removed))
      }
    }
    case _ => // nil
  }

  // optional
  /** `expected` is the set of ActorRefs whose replies we are waiting for,
    * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
    */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    case CopyFinished =>
      if (expected.size <=1 && expected(sender) && insertConfirmed) {
        context.parent ! CopyFinished
        context.stop(self)
      } else {
        context.become(copying(expected - sender, insertConfirmed))
      }
    case o: OperationFinished => {
      if (expected.isEmpty) {
        context.parent ! CopyFinished
        context.stop(self)
      } else {
        context.become(copying(expected, true))
      }
    }
    case t =>
      log.debug(s"PROBLEM:BinaryTreeNode($elem,$removed)!_$t")
  }


