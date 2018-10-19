package com.digitalpanda.scala.playground

import scala.io.Source

object HelloWorld {
  def main(args: Array[String]): Unit = {
    //chapter_2_first_steps_in_Scala(args)
    //chapter_3_next_steps_in_Scala(args)
    //chapter_4_classes_and_objects(args)
    chapter_5_basic_types_and_operations(args)

  }

  def chapter_2_first_steps_in_Scala(args : Array[String]): Unit = {
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
    args.foreach((arg: String) => println(arg))
    for (arg <- args)
      println(arg)

    /* Run a .scala script/notebook:
        > scala examples.scala
        //examples.scala:
          println("Hello, " + args(0) + "!") */
    }

    def chapter_3_next_steps_in_Scala(args : Array[String]): Unit = {

      //instantiate an object:
      val big = new java.math.BigInteger("123")

      //Array (mutable) instantiation and assignment
      val greetingArr = new Array[String](3)
      greetingArr(0) = "Hello"
      greetingArr(2) = " World!"
      for (i <- 0 to 2) print(greetingArr(i))
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
      val fullList = beginList ::: endList
      println(fullList + "is a new list")

      //":" method invoked on the right operand if method name finishes with char ':'
      var beginPrefixedList = 1 :: beginList
      // is equivalent to
      beginPrefixedList = beginList.::(1)
      //---
      var strange = 1 :: 2 :: Nil
      // is equivalent to
      strange = Nil.::(2).::(1)

      //Immutable tuples : can store a sequence of objects of different types (max tuple size == 22)
      val pair = (99, "plops")
      println(pair._2) // Access public field "_2" of the tuple object ...

      //Sets and Maps can be either scala.collection.mutable or  scala.collection.immutable
      var jetSet = Set("Boeing", "Airbus") //by default the immutable set is used (otherwise use explicit import)
      jetSet += "Lear" // <=> jetSet = jetSet.+("lear") // returns a new immutable set
      println(jetSet.contains("Cessna"))

      val romanNumeral = Map(1 -> "I", 2 -> "II", 3 -> "III", 4 -> "IV", 5 -> "V")
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

      //Read and print lines from resource file
      val resourceFilePath = "textFile1.txt"
      def widthOfLength(str: String) = str.length.toString.length
      println("\nRead lines from resource file \"" + resourceFilePath + "\":")
      val lines = Source.fromResource(resourceFilePath).getLines().toList
      val longestLine = lines.reduceLeft((a, b) => if (a.length > b.length) a else b)
      val maxWidth = widthOfLength(longestLine)
      for (line <- lines) {
        val numSpaces = maxWidth - widthOfLength(line)
        val padding = " " * numSpaces
        println(padding + line.length + " | " + line)

      }
    }


  class ChecksumAccumulator {
    // class definition goes here.. Inside a class definition, you place fields and methods, which are collectively called members.

    //Field is an instance variable, default access level is public
    private var sum = 0

    //Method parameters are val by default (final in java)
    def add(b: Byte) { sum += b } // Procedure like braces for unit function => no "=" symbol required
    def checksum(): Int = ~ (sum & 0xFF) + 1
  }

  def chapter_4_classes_and_objects(args: Array[String]): Unit = {
    //=> 4.1 Classes, fields, and methods
    //Class definition:  See ChecksumAccumulator.scala

    //Create new class objects
    val acc = new ChecksumAccumulator
    val csa = new ChecksumAccumulator
    //The sum Field is a ChecksumAccumulator instance variable
    acc add 3
    println("acc.checksum(): " + acc.checksum())
    println("csa.checksum(): " + csa.checksum())

    //The last line of a function is the returned result, if the return is unit, the last line is cast to unit and thus forgotten
    def f(): Unit = "this String gets lost"
    def g() { "this String gets lost too" }
    def h(): String = { "this String gets lost too" } // The "=" sign affects the last value of the function as return result
    f()
    g()
    h()

    //=> 4.2 Semicolon inference
    // Semicolon are usually optional

    //=> 4.3 Singleton objects
    // See ChecksumAccumulator.scala : singleton "object" with  his companion class

    //=> 4.4 A Scala application
    // See Summer object

    //=> 4.5 Application trait (do not use)
    // See FallWinterSpringSummer object
  }


  def chapter_5_basic_types_and_operations(args: Array[String]): Unit = {
    //=> 5.1 Some basic types
    //Scala’s basic types have the exact same ranges as the corresponding types in Java => direct cast to java primitive types
    //Java’s basic types and operators have the same meaning in Scala. In the following, will only show differences

    //Raw string allow to include anything without the need of escaping:
    println(
      """Welcome to Ultamix 3000.
        Type "HELP" for help.""")
    //Use stripMargin and | delimiters to avoid unwished lines before the |
    println(
      """|Welcome to Ultamix 3000.
        |Type "HELP" for help.""".stripMargin)
    val escapesExample = "\\\"\'"
    println(escapesExample)

    //Symbols (literals used as identifiers for a key field)
    //symbols are "interned" :  If you write the same symbol literal twice, both expressions will refer to the exact same Symbol object.
    val symbol: Symbol = 'symbol
    println(symbol.name)

    //Operators (notation)
    //operators are actually just a nice syntax for ordinary method calls:
    //INFIX operators :
    var sum = 1 + 2 // Scala invokes (1).+(2); // infix "+" operator : operands are on its left and right ....
    sum = 1.+(2)
    var otherExample = "plop" indexOf 'o' // Scala invokes "plop".indexOf(’o’)
    otherExample = "plop"  indexOf ('o', 5) // Scala invokes "plop".indexOf(’o’, 5)
    //PREFIX operator:
    //The only identifiers that can be used as prefix operators are + , - , ! , and ~ .
    var num = -2 // "-" is a prefix operator equvalent to :
    num = 2.unary_-
  }
}