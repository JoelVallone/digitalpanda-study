package com.digitalpanda.scala.playground

import com.digitalpanda.scala.playground.circuit.CircuitSimulation

import scala.collection.immutable.{Queue, TreeMap, TreeSet}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


object HelloWorld17 {


  def chapterSeparator(chapter: Array[String] => Unit, chapterNumber: Int)(args: Array[String]): Unit = {
    println("===> CHAPTER " + chapterNumber + " <===")
    chapter(args)
    println()
    println()
  }

  def main(args: Array[String]): Unit = {

    chapterSeparator(chapter_17_collections,17) { println("plop"); args }
    chapterSeparator(chapter_18_stateful_objects,18){ args }
    chapterSeparator(chapter_19_type_parametrization,19)( args )
  }


  def chapter_19_type_parametrization(args: Array[String]): Unit = {
    //We’re presenting type parameterization and information hiding together,
    // because information hiding can be used to obtain more general type parameterization variance annotations.
    //Type parameterization allows you to write generic classes and traits.
    //VARIANCE defines inheritance relationships of parameterized types, such as whether a Set[String],
    // for example, is a subtype of Set[AnyRef].

    //=> 19.1 Functional queues
    //Unlike a mutable queue, a functional queue does not change its contents when an element is appended.
    val q = Queue(1, 2, 3)
    val q1 = q enqueue 4
    println("q: " + q)
    println("q1: " + q1)
    //Where a list is usually extended at the front, using a :: operation, a queue is extended at the end, using enqueue.
    //All three operations head, tail, and enqueue should operate in constant time.
    //The idea is to represent a queue by two lists (aka stack....), called leading and trailing :
    class Queue[T](
                    private val leading: List[T],
                    private val trailing: List[T]
                  ) {

      private def mirror = if (leading.isEmpty) new Queue(trailing.reverse, Nil) else this

      def head = mirror.leading.head

      def tail = {
        val q = mirror
        new Queue(q.leading.tail, q.trailing)
      }

      def enqueue(x: T) =
        new Queue(leading, x :: trailing)
    }
  }

  def chapter_18_stateful_objects(args: Array[String]): Unit = {
    //Such stateful objects often come up naturally when you want to model objects in the real world
    // that change over time.

    //=> 18.1 What makes an object stateful?
    //For a stateful object, on the other hand, the result of a method call or field access may depend on what
    // operations were previously performed on the object. (not the case for immutable)

    //=> 18.2 Re-assignable variables and properties
    //You can perform two fundamental operations on a re-assignable variable: get its value or set it to a new value.
    //In Scala, every var that is a non-private mem- ber of some object implicitly defines a getter and a setter
    // method with it. These getters and setters are named differently from the Java convention, however.
    // The getter of a var x is just named “x”, while its setter is named “x_=”.
    class Time {
      var hour = 12
      var minute = 0
    }
    //Equivalent explicit class
    class TimeExplicit {
      private[this] var h = 12
      private[this] var m = 0
      def hour: Int = h
      def hour_=(x: Int) { h = x }
      def minute: Int = m
      def minute_=(x: Int) { m = x }
    }
    // By defining these access methods directly you can interpret the
    // operations of variable access and variable assignment as you like:
    class TimeExplicitWithChecks {
      private[this] var h = 12
      private[this] var m = 0
      def hour: Int = h
      def hour_= (x: Int) {
        require(0 <= x && x < 24)
        h=x }
      def minute = m
      def minute_= (x: Int) {
        require(0 <= x && x < 60)
        m=x }

      override def toString: String = "hour: " + hour + ", minute: " + minute
    }
    var time = new TimeExplicitWithChecks
    println("time: " + time)
    time.hour = 15
    println("time: " + time)
    //Scala’s convention of always interprets a variable x as a pair of setter x_= and getter x methods
    //Getter ans setter without directly associated field :
    class Thermometer {
      var kelvin: Float = _ // <- assigns the zero-value for the target type
      def celsius = kelvin - 273.15F
      def celsius_= (c: Float): Unit = {
        kelvin = c + 273.15F
      }
      def fahrenheit = celsius * 9 / 5 + 32
      def fahrenheit_= (f: Float) {
        celsius = (f - 32) * 5 / 9
      }
      override def toString = celsius +"C / " + kelvin + "K / " + fahrenheit + "F"
    }
    var thermometer = new Thermometer
    println("thermometer: " + thermometer)

    //=> 18.3 Case study: Discrete event simulation
    //The rest of this chapter shows by way of an extended example how stateful objects can be combined with
    // first-class function values in interesting ways.
    //- See the classes in the circuit package
    //A concrete circuit simulation will be an object that inherits from class CircuitSimulation:
    println("\nInitialize circuit")
    object MySimulation extends CircuitSimulation {
      def InverterDelay = 1
      def AndGateDelay = 3
      def OrGateDelay = 5
    }
    import MySimulation._
    val input1, input2, sum, carry = new Wire
    probe("sum", sum)
    probe("carry", carry)
    halfAdder(input1, input2, sum, carry)
    input1 setSignal true
    run()
    input2 setSignal true
    run()
  }

  def chapter_17_collections(args: Array[String]): Unit = {
    //=> 17.1 Sequences
    //Sequences types let you work with groups of data lined up in order

    // - Lists
    //Lists sup- port fast addition and removal of items to the beginning of the list,
    // but they do not provide fast access to arbitrary indexes because the implementation
    // must iterate through the list linearly.

    // - Arrays
    //Arrays allow you to hold a sequence of elements and efficiently access an element at an arbitrary position.

    // - List buffers
    //A ListBuffer is a mutable object (contained in package scala.collection.mutable),
    // which can help you build lists more efficiently when you need to append.
    // ListBuffer provides constant time ap- pend and prepend operations.
    // You append elements with the += operator, and prepend them with the +=: operator.
    val buf = new ListBuffer[Int]
    buf += 1;
    buf += 2
    3 +=: buf
    println("buf: " + buf)
    println("buf.toList: " + buf.toList)

    // - Array buffers
    //An ArrayBuffer is like an array, except that you can additionally add and remove elements from
    // the beginning and end of the sequence.
    val arrBuf = new ArrayBuffer[Int]()
    arrBuf += 12;
    arrBuf += 15
    println("arrBuf: " + arrBuf)
    println("arrBuf(0): " + arrBuf(0))

    // - Strings
    //Because Predef has an implicit conversion from String to StringOps, you can treat any string like a sequence.
    val str = "Robert Frost"
    println("s.exists(_.isUpper): " + str.exists(_.isUpper))
    //Because no method named “exists” is declared in class String itself, the Scala compiler will implicitly converts
    // to StringOps, which has the method.


    //=> 17.2 Sets and maps
    //By default when you write “Set” or “Map” you get an immutable object.
    // If you want the mutable variant, you need to do an explicit import.
    //The “type” keyword is used in Predef objec t to define Set and Map as aliases for the longer fully qualified
    // names of the immutable set and map traits.
    //If you want to use both mutable and immutable sets or maps in the same source file:
    // - Using sets
    import scala.collection.mutable
    val text = "See Spot run. Run, Spot. Run!"
    val words = mutable.Set.empty[String]
    for (word <- text.split("[ !,.]+")) words += word.toLowerCase
    println("words: " + words)

    // - Using Maps
    val map = mutable.Map.empty[String, Int]
    map("hello") = 1
    map("there") = 2
    println("map: " + map)
    println("map(\"hello\"): " + map("hello"))

    def countWords(text: String) = {
      val counts = mutable.Map.empty[String, Int]
      for (rawWord <- text.split("[ ,!.]+")) {
        val word = rawWord.toLowerCase
        val oldCount = if (counts.contains(word)) counts(word) else 0
        counts += (word -> (oldCount + 1))
      }
      counts
    }
    println("countWords(text): " + countWords(text))

    //- Default sets and maps
    //-> Mutable Set and Map factories return HashSet and HasMap
    //-> For immutable sets with fewer than five elements, a special class devoted exclusively to sets of each particular
    // size is used, to maximize performance. Once you request a set that has five or more elements in it,
    // however, the factory method will return an implementation that uses hash tries. Idem for immutable map.

    //- Sorted sets and maps
    //For this purpose, the Scala collections library provides traits SortedSet and SortedMap.
    // These traits are implemented by classes TreeSet and TreeMap, which use a red-black tree to keep elements
    // (in the case of TreeSet) or keys (in the case of TreeMap) in order.
    val treeSet = TreeSet(9, 3, 1, 8, 0, 2, 7, 4, 6, 5)
    println("treeSet: " + treeSet)
    var treeMap = TreeMap(3 -> 'x', 1 -> 'x', 4 -> 'x')
    treeMap ++= List(2 -> 'x')
    println("treeMap: " + treeMap)

    //=> 17.3 Selecting mutable vs immutable collections
    //When in doubt, it is better to start with an immutable collection and change it later if you need to,
    // because immutable collections can be easier to reason about than mutable ones............... bof
    //Besides being potentially easier to reason about, immutable collections can usually be stored more compactly
    // than mutable ones if the number of elements stored in the collection is small.
    var capitalImmutable = Map("US" -> "Washington", "France" -> "Paris")
    capitalImmutable += ("Japan" -> "Tokyo") // if "var" is used, is translated to: capitalImmutable = capitalImmutable + ...
    println("capitalImmutable: " + capitalImmutable)


    //=> 17.4 Initializing collections
    // - Converting to array or list
    //When you invoke toList or toArray on a collection, the order of the elements in the resulting list or array will
    // be the same as the order of elements produced by an iterator obtained by invoking elements on that collection.
    //Keep in mind, however, that conversion to lists or arrays usually requires copying all of the elements of
    // the collection, and thus may be slow for large collections.
    println("treeSet.toList: " + treeSet.toList)

    // - Converting between mutable and immutable sets and maps
    //Create a collection of the new type using the empty method and then add the new elements using either ++ or ++=,
    // whichever is appropriate for the target collection type.
    val mutaSet = mutable.Set.empty ++= treeSet.toList
    val immutaSet = Set.empty ++ mutaSet
    println("mutaSet: " + mutaSet)
    println("immutaSet: " + immutaSet)


    //=> 17.5 Tuples
    //Unlike an array or list, a tuple can hold objects with different type.
    println("Tuple (1, \"hello\", Console): " +  String.valueOf((1, "hello", 2.4)))
    //Tuples save you the tedium of defining simplistic data-heavy classes.
    def longestWord(words: Array[String]) = {
      var word = words(0)
      var idx = 0
      for (i <- 1 until words.length)
        if (words(i).length > word.length) {
          word = words(i)
          idx = i
        }
      (word, idx)
    }
    val longestAsTuple = longestWord("The quick brown fox".split(" "))
    println("longestWord(\"The quick brown fox\".split(\" \")): " + longestAsTuple)
    println("longestAsTuple._1" + longestAsTuple._1)
    val longest, idx = longestAsTuple
    //This syntax gives MULTIPLE DEFINITIONS (longest and idx here) of the same expression (on the right side)
    //Whenever the combination has some meaning beyond “an A and a B", or you want to add some methods to the
    // combination, it is better to go ahead and create a class.
  }


}