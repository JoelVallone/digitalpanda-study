package frp

//Immutable signal depending on expr
class Signal[T](expr: => T) {

  import Signal._
  private var myExpr: () => T = _ //Current expression defining myValue
  private var myValue: T = _ // Current signal value
  private var observers: Set[Signal[_]] = Set() //Other signals depending on myValue
  update(expr) //Init signal

  //Called with: val v1 = Var(3); v1() = 4
  protected def update(expr: => T): Unit = {
    myExpr = () => expr //Only evaluate when called
    computeValue()
  }


  protected def computeValue(): Unit = {
    val newValue = caller.withValue(this)(myExpr())

    //Notify  all callers/observers if callee's value changed
    // => propagates callee's calue change to dependent caller signals
    if (myValue != newValue) {
      myValue = newValue
      val obs = observers
      //Observers will add themselves if their expression still contains the callee
      observers = Set()
      obs.foreach(_.computeValue())
    }
  }

  //Called with: val s1 = Signal{ v1() + 1}; s1()
  def apply() = {
    //The caller just registered himself into headSignal before calling apply()
    // => caller is added to the observer
    observers += caller.headSignal

    assert(!caller.headSignal.observers.contains(this), "cyclic signal definition")
    myValue
  }
}

object Signal {
  //Need to register which caller Signals depends on this Signal instance
  // when it gets updated. <=> global caller stack
  //A caller signal is a signal with this Signal in its expression.
  // Ex:> val v1 = Var(3); s1 = Signal{ v1() + 1}; s1(); // s1 is a caller of v1
  val caller = new StackableVariable[Signal[_]](NoSignal) //Init with NoOp Signal
  def apply[T](expr: => T) = new Signal(expr)

  class StackableVariable[T](init: T) { //T == Signal[_]
    private var signals: List[T] = List(init)
    def headSignal: T = signals.head
    def withValue[R](newHead: T)(op: => R): R = {
      //Register in head of caller stack before calling op()
      signals = newHead :: signals
      try op finally signals = signals.tail
        //Call to op expression will trigger contained signals apply()
        // before pop of op caller.
        //Callees will thus have a reference to their direct caller via
        // the global caller stack head !
    }
  }
}
