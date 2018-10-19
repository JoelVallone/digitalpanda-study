package com.digitalpanda.scala.playground

object HelloWorld {
  def main(args: Array[String]): Unit = {
    chapter_2_first_steps_in_Scala(args)

  }

  def chapter_2_first_steps_in_Scala(args : Array[String]): Unit ={
    println("Hello, world!")
    //variable:
    var msg: String = "plop"

    //constant: Prefer usage of val with immutable data. Use var with imperative style when better suited (See chapter 7)
    val msgConst: String = "plop2"

    //function:
    def max(x: Int, y: Int): Int = {
      if (x > y) x
      else y
    }

    //for loop
    args.foreach(println)
    args.foreach( (arg: String) => println(arg))
    for (arg <- args)
      println(arg)

    /* Run a .scala script/notebook:
        > scala examples.scala
        //examples.scala:
          println("Hello, " + args(0) + "!") */


    //instantiate an object:
    val big = new java.math.BigInteger("123")

    //Array (mutable) instantiation and assignment
    val greetingArr = new Array[String](3)
    greetingArr(0) = "Hello"
    greetingArr(2) = " World!"
    for ( i <- 0 to 2) print(greetingArr(i))
    var numNames = Array("zero", "one") // Factory method of Array <=> Array.apply("zero","one")
    numNames = Array.apply("zero", "one")

    //calls on a variable
    greetingArr.update(1, ", ")
    println(greetingArr.apply(2))

    //Generate Int Sequence
    var numbers = 0 to 2
    numbers = 0.to(2)

    //Immutable List
    val beginList = List(1, 2)
    val endList = List(3, 4)
    val fullList = beginList:::endList
    println(fullList + "is a new list")

    //":" method invoked on the right operand if method name finishes with char ':'
    var beginPrefixedList = 1::beginList
    // is equivalent to
    beginPrefixedList = beginList.::(1)
    //---
    var strange = 1::2::Nil
    // is equivalent to
    strange = Nil.::(2).::(1)

    //Immutable tuples : can store a sequence of objects of different types (max tuple size == 22)
    val pair = (99, "plops")
    println(pair._2) // Access public field "_2" of the tuple object ...

    //Sets and Maps can be either scala.collection.mutable or  scala.collection.immutable
    var jetSet = Set("Boeing", "Airbus") //by default the immutable set is used (otherwise use explicit import)
    jetSet += "Lear" // <=> jetSet = jetSet.+("lear") // returns a new immutable set
    println(jetSet.contains("Cessna"))

    val romanNumeral = Map( 1 -> "I", 2 -> "II", 3 -> "III", 4 -> "IV", 5 -> "V" )
    println(romanNumeral(4))

    /*Imperative vs Functional style
        ==> Imperative (uses var, mutables, explicit step by step instructions -> what and how)
        -> more verbose*/
    def printArgsImperative(args: Array[String]): Unit = {
      var i = 0
      while (i < args.length) {
        println(args(i))
        i += 1
      }
    }
    printArgsImperative(Array("printArgsImperative1", "printArgsImperative2"))
    //==> Functional (uses val, immutables, declarative -> what to do, not how, function calls with no side effect (If Unit then probable side effect, easy to test when no side effects just check return value)
    def printArgs(args: Array[String]): Unit = args.foreach(println) // still has the side effect of printing
    val strings = Array("printArgsFunctional1", "printArgsFunctional2")
    println(printArgs(strings))
    //OR:
    def formatArgs(args: Array[String]) = args.mkString("\n")
    println(formatArgs(strings))
    //assertion check throws exception if false (used for testing)
    assert(formatArgs(strings) == "printArgsFunctional1\nprintArgsFunctional2")
//


  }
}
