package com.digitalpanda.scala.playground.circuit

/*
  A discrete event simulation performs user-defined actions at specified times.
  The actions, which are defined by concrete simulation subclasses, all share a common type:
  type Action = () => Unit

 */
abstract class Simulation {

  type Action = () => Unit

  case class WorkItem(time: Int, action: Action)

  private var curtime = 0

  def currentTime: Int = curtime

  private var agenda: List[WorkItem] = List()

  private def insert(ag: List[WorkItem], item: WorkItem): List[WorkItem] = {
    if (ag.isEmpty || item.time < ag.head.time) item :: ag else ag.head :: insert(ag.tail, item)
  }

  //The power of the simulation framework comes from the fact that actions stored in work items can themselves
  // install further work items into the agenda when they are executed.
  def afterDelay(delay: Int)(block: => Unit) { // the block is passed by name => lazy-evaluated
    val item = WorkItem(currentTime + delay, () => block)
    agenda = insert(agenda, item)
  }

  private def next() {
    (agenda: @unchecked) match { // <- given the call pattern, the match is safe even if it is non-exhaustive.
      case item :: rest =>
        agenda = rest
        curtime = item.time
        item.action()
    }
  }

  def run() {
    afterDelay(0) {
      println("*** simulation started, time = " + currentTime + " ***")
    }
    while (!agenda.isEmpty) next()
  }
}