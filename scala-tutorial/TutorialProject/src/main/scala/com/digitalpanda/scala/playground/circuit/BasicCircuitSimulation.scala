package com.digitalpanda.scala.playground.circuit

abstract class BasicCircuitSimulation extends Simulation {

  def InverterDelay: Int // <- first letter is capitalised as it is considered a constant
  def AndGateDelay: Int
  def OrGateDelay: Int

  class Wire {

    private var sigVal = false

    private var actions: List[Action] = List()

    def getSignal = sigVal

    def setSignal(s: Boolean) =
      if (s != sigVal) {
        sigVal = s
        actions foreach (_ ()) // <- "_ ()" is a shorthand for "f => f ()"
      }

    def addAction(a: Action) = {
      actions = a :: actions
      a()
    }
  }


  //What’s unusual, given the functional emphasis of Scala, is that these procedures construct the gates as a side-effect,
  // instead of returning the constructed gates as a result. For instance, an invocation of inverter(a, b) places
  // an inverter between the wires a and b. It turns out that this side-effecting construction makes it easier
  // to construct complicated circuits gradually.
  //Class Wire and functions inverter, andGate, and orGate represent a little language with which users can define
  // digital circuits. It’s a good example of an internal DSL, a domain specific language defined as a library in
  // a host language instead of being implemented on its own.
  def inverter(input: Wire, output: Wire) = {
    def invertAction() {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) {
        output setSignal !inputSig
      }
    }
    input addAction invertAction
  }

  def andGate(a1: Wire, a2: Wire, output: Wire) = {
    def andAction() = {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) {
        output setSignal (a1Sig & a2Sig)
      }
    }
    a1 addAction andAction
    a2 addAction andAction
  }
  def orGate(o1: Wire, o2: Wire, output: Wire) {
    def orAction() {
      val o1Sig = o1.getSignal
      val o2Sig = o2.getSignal
      afterDelay(OrGateDelay) {
        output setSignal (o1Sig | o2Sig)
      }
    }
    o1 addAction orAction
    o2 addAction orAction
  }
  def probe(name: String, wire: Wire) {
    def probeAction() {
      println(name + " " + currentTime + " new-value = " + wire.getSignal)
    }
    wire addAction probeAction
  }
}
