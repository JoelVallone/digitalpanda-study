package com.digitalpanda.scala.playground

import java.io._

import scala.util.control.Breaks._
import java.net.{MalformedURLException, URL}

import scala.io.Source

object HelloWorld {


  def chapterSeparator(chapter: Array[String] => Unit, chapterNumber: Int)(args: Array[String]): Unit = {
    println("===> CHAPTER " + chapterNumber + " <===")
    chapter(args)
    println()
    println()
  }

  def main(args: Array[String]): Unit = {

    val chapter_two_with_formatting = chapterSeparator(chapter_2_first_steps_in_Scala, 2) _
    chapter_two_with_formatting(args)
    chapterSeparator(chapter_3_next_steps_in_Scala,3)(args)
    chapterSeparator(chapter_4_classes_and_objects,4)(args)
    chapterSeparator(chapter_5_basic_types_and_operations,5)(args)
    chapterSeparator(chapter_6_functional_objects,6)(args)
    chapterSeparator(chapter_7_built_in_control_structures,7)(args)
    chapterSeparator(chapter_8_functions_and_closures,8)(args)
    chapterSeparator(chapter_9_control_abstraction,9)(args)
    chapterSeparator(chapter_10_composition_and_inheritance,10)(args)
  }

  def chapter_10_composition_and_inheritance(args: Array[String]): Unit = {
    //Go into the details of Scala’s support for object-oriented programming.
    // We’ll compare two fundamental relationships between classes: composition and inheritance.
    // Composition means one class holds a reference to another, using the referenced class to help it fulfill
    // its mission. Inheritance is the superclass/subclass relationship.

    //10.1 A two-dimensional layout library
    //As a running example in this chapter, we’ll create a library for building and rendering two-dimensional
    // layout elements. Each element will represent a rectangle filled with text.
  }

  def chapter_9_control_abstraction(args: Array[String]): Unit = {
    //In this chapter, we’ll show you how to apply function values to create new control abstractions.
    // Along the way, you’ll also learn about currying and by-name parameters

    //=> 9.1 Reducing code duplication
    //HIGHER-ORDER FUNCTIONS: functions that take functions as parameters.
    // They give you extra opportunities to condense and simplify code.
    // One benefit of higher-order functions is they enable you to create control abstractions
    // that allow you to reduce code duplication.
    object FileMatcher {

      private def filesHere = new java.io.File(".").listFiles

      def filesEnding(query: String) =
        filesMatching((fileName: String) => fileName.endsWith(query)) // <= Same as : _.endsWith(query)

      def filesContaining(query: String) =
        filesMatching((fileName) => fileName.contains(query)) // <= Same as : _.contains(query)

      def filesRegex(query: String) =
        //The underscore is a placeholder for the gile name parameter.
        filesMatching(_.matches(query))

      def filesMatching(matcher: String => Boolean) =
        for (file <- filesHere; if matcher(file.getName))
          yield file

    }

    //=> 9.2 Simplifying client code
    // By using higher order functions.
    def containsNegIter(nums: List[Int]): Boolean = {
      var exists = false
      for (num <- nums)
        if (num < 0)
          exists = true
      exists
    }
    println(containsNegIter(List(1, 2, -3, 4)))
    /*
      The exists method bellow represents a control abstraction relying on higher order functions (input of exists).
      It is a special-purpose looping construct provided by the Scala library rather than being built into the Scala
      language like while or for.
      In the previous section, the higher-order function, filesMatching, reduces code duplication in the implementation
      of the object FileMatcher. The exists method provides a similar benefit, but because exists is public in
      Scala’s collections API, the code duplication it reduces is client code of that API.
     */
    def containsNeg(nums: List[Int]): Boolean = nums.exists(_ < 0)
    println(containsNeg(List(1, 2, -3, 4)))

    //=> 9.3 Currying
    //CURRYING: A way to write functions with multiple parameter lists.
    // For instance def f(x: Int)(y: Int) is a curried function with two parameter lists.
    // A curried function is applied by passing several arguments lists, as in: f(3)(4).
    // However, it is also possible to write a partial application of a curried function, such as f(3).
    def plainOldSum(x: Int, y: Int) = x + y
    println(plainOldSum(1, 2))
    def curriedSum(x: Int)(y: Int) = x + y
    println(curriedSum(1)(2))
    //When you invoke curriedSum, you actually get two traditional function invocations back to back.
    // The first function invocation takes a single Int parameter named x,
    // and returns a function value for the second function. This second function takes the Int parameter y.
    // THe currying process is similar to the following:
    def first(x: Int) = (y: Int) => x + y
    var second = first(1)
    println(second(2))
    //The underscore in curriedSum(1) _ is a placeholder for the second parameter list
    val secondSecond = curriedSum(1) _
    println(secondSecond(2))

    //=> 9.4 Writing new control structures
    //In languages with first-class functions, you can effectively make new control structures even though
    // the syntax of the language is fixed. All you need to do is create methods that take functions as arguments.
    def twice(op: Double => Double, x: Double) = op(op(x))
    println(twice(_ + 1, 5))
    //Any time you find a control pattern repeated in multiple parts of your code,
    // you should think about implementing it as a new control structure.
    // Example : open a resource, operate on it, and then close the resource.
    def withPrintWriterOldSchool(file: File, op: PrintWriter => Unit) {
      val writer = new PrintWriter(file)
      try {
        op(writer)
      } finally {
        writer.close()
      }
    }
    //The LOAN PATTERN, because a control-abstraction function, such as withPrintWriter,
    // opens a resource and “loans” it to a function. For instance, withPrintWriter in the previous example
    // loans a PrintWriter to the function, op. When the function completes,
    // it signals that it no longer needs the “borrowed” resource.
    withPrintWriterOldSchool(
      new File("date.txt"),
      writer => writer.println(new java.util.Date)
    )
    //One way in which you can make the client code look a bit more like a built-in control structure is
    // to use curly braces instead of parentheses to surround the argument list.
    //In any method invocation in Scala in which you’re passing in exactly ONE argument,
    // you can opt to use curly braces to surround the argument instead of parentheses.
    //The purpose of this ability to substitute curly braces for parentheses for passing in one argument is to
    // enable client programmers to write function literals between curly braces : { (a:Int, b:Int) => a + b }
    println("Hello, world!")
    println { "Hello, world!" }
    def withPrintWriter(file: File)(op: PrintWriter => Unit) {
      val writer = new PrintWriter(file)
      try {
        op(writer)
      } finally {
        writer.close()
      }
    }
    val file = new File("date.txt")
    withPrintWriter(file) {
      writer => writer.println(new java.util.Date)
    }
    //=> 9.5 By-name parameters
    var assertionsEnabled = true
    def myAssert(predicate: () => Boolean) =
      if (assertionsEnabled && !predicate())
        throw new AssertionError
    myAssert{ () => 5 > 3 }
    //  myAssert(5 > 3) // <= Not allowed
    //BY NAME PARAMETER : when function to pass has no input.
    //  A by-name type, in which the empty parameter list, (), is left out, is only allowed for parameters.
    //  Lazy evaluation : evaluate by name expression when called into and not when input to the higher order function.
    def byNameAssert(predicate: => Boolean) =
      if (assertionsEnabled && !predicate)
        throw new AssertionError
    byNameAssert{5 > 3}
    //Nevertheless, one difference exists between these two approaches (byNameAssert, boolAssert) that is important to note.
    // Because the type of boolAssert’s parameter is Boolean, the expression inside the parentheses in
    // boolAssert(5 > 3) is evaluated be- fore the call to boolAssert.
    // The expression 5 > 3 yields true, which is passed to boolAssert.
    // By contrast, because the type of byNameAssert’s predicate parameter is => Boolean,
    // the expression inside the parentheses in byNameAssert(5 > 3) is not evaluated before the call to byNameAssert.
    // Instead a function value will be created whose apply method will evaluate 5 > 3,
    // and this function value will be passed to byNameAssert.
    def boolAssert(predicate: Boolean) =
      if (assertionsEnabled && !predicate)
        throw new AssertionError
    boolAssert(5 > 3)

    assertionsEnabled = false
    def byNameAssert2(predicate: => Boolean) =
      if (assertionsEnabled && !predicate)
        throw new AssertionError
    def boolAssert2(predicate: Boolean) =
      if (assertionsEnabled && !predicate)
        throw new AssertionError
    val x = 1
    byNameAssert2(x / 0 == 0)
    try {
      boolAssert2(5 > 3)
    } catch {
      case e : Exception => e.printStackTrace()
    }
  }


  def chapter_8_functions_and_closures(args: Array[String]): Unit = {
    // Programs should be decomposed into many small functions that each do a well-defined task.
    // Individual functions are often quite small.
    // Each building block should be simple enough to be understood individually.

    //=> 8.1 Methods
    //Methods, which are functions that are members of some object.

    //=> 8.2 Local functions
    /*Helper function names can pollute the program namespace.
    In Java, your main tool for this purpose is the private methods (also available in scala)
    Other approach -> Local functions: define functions inside other functions. Just like local variables
    Local functions can access the parameters of their enclosing function.
    */
    def processFile(filename: String, width: Int) {
      def processLine(line: String) {
        if (line.length > width)
          println(filename +": "+ line)
      }
      val source = Source.fromResource(filename)
      for (line <- source.getLines())
        processLine(line)
    }
    processFile("textFile1.txt", 10)

    //=> 8.3 First-class functions
    //One can write down functions as unnamed literals and then pass them around as values.
    //FUNCTION LITERAL: A function with no name in Scala source code: (x: Int, y: Int) => x + y
    // A function literal is compiled into a class that when instantiated at runtime is a function value.
    //FUNCTION VALUE: A function object that can be invoked just like any other function.
    //  A function value is “invoked” when its .apply() method is called.
    //  Every function value is an instance of some class that extends one of several FunctionN traits in package scala
    val increase = (x: Int) => x + 1
    println(increase(3))
    val someNumbers = List(-11, -10, -5, 0, 5, 10)
    someNumbers.foreach((x: Int) => print(x + ","))
    println

    //=> 8.4 Short forms of function literals
    //Allow to remove clutter from your code.
    //Start by writing a function literal without the argument type,
    // and, if the compiler gets confused, add in the type.
    //TARGET TYPING : the targeted usage of an expression is allowed to influence the typing of that expression
    //Leave out parentheses around a parameter whose type is inferred.
    someNumbers.filter(x => x < 0).foreach(print)
    println

    //=> 8.5 Placeholder syntax
    //Use underscores as placeholders for one or more parameters,
    // so long as each parameter appears only one time within the function literal.
    // Multiple underscores mean multiple parameters, not reuse of a single parameter repeatedly
    someNumbers.filter(_ < 0).foreach(print)
    println
    var f = (x: Int, y :Int)  =>  x + y
    println(f(5, 10))
        f = (_: Int) + (_: Int)
    println(f(5, 10))

    //=> 8.6 PARTIALLY APPLIED FUNCTION: A function that’s used in an expression and that misses some of its arguments.
    // For instance, if function f has type Int => Int => Int, then f and f(1) are partially applied functions.
    def sum(a: Int, b: Int, c: Int) = a + b + c
    val a = sum _ // <= This is as a way to transform a def into a function value (partial function in this case)
    val b = sum (1,_:Int,3) // <= This is as a way to transform a def into a function value (partial function in this case)
    //The Scala compiler instantiates a function value that takes the three integer parameters missing
    // from the partially applied function expression, sum _, and assigns a reference to that new function
    // value to the variable a. When you apply three arguments to this new function value,
    // it will turn around and invoke sum, passing in those same three arguments:
    println(a(1, 2, 3))
    println(a.apply(1, 2, 3))
    println(b(2))
    println(sum(1, 2, 3)) //but not sum.apply() as it is not an instantiated function value (object with apply method implementing trait FunctionN)

    //=> 8.7 Closures
    //A FREE VARIABLE of an expression is a variable that’s used inside the expression but not defined inside the expression
    //A BOUND VARIABLE of an expression is a variable that’s both used and defined inside the expression.
    //The function value (the object) that’s created at runtime from this function literal is called a CLOSURE.
    //The name arises from the act of “closing” the function literal by “capturing” the bindings of its free variable
    //The resulting function value, which will contain a reference (=>outside change reflected) to the captured more variable,
    //The instance used in the closure is the one that was active at the time the closure was created
    def makeIncreaser(more: Int) = (x: Int) => x + more
    val inc1 = makeIncreaser(1)
    val inc9999 = makeIncreaser(9999)
    println(inc1(10))
    println(inc9999(10))

    //=> 8.8 Special function call forms
    //==> Repeated parameters
    //The last parameter to a function may be repeated.
    // This allows clients to pass variable length argument lists to the function.
    // The type of the repeated parameter is an Array of the declared type of the parameter.
    def echo(args: String*) =
      { for (arg <- args) print(arg + " "); println() }
    echo("One!")
    echo("One", "Two", "Three")
    val arr = Array("What's", "up", "doc?")
    // Pass each element of arr as its own argu- ment to echo, rather than all of it as a single argument
    echo(arr: _*)
    echo(arr: _*)

    //==> Named arguments
    //The syntax is simply that each argument is preceded by a parameter name and an equals sign.
    def speed(nil: Int, distance: Float, time: Float): Float =
      distance / time
    println(speed(42, 100,10))
    //It is also possible to mix positional and named arguments.
    // In that case, the positional arguments come first.
    println(speed(42, time = 10, distance = 100))

    //==> Default parameter values
    //Default parameters are especially helpful when used in combination with named parameters
    def printTime(out: java.io.PrintStream = System.out, divisor: Int = 1) =
      out.println("time = "+ System.currentTimeMillis()/divisor)
    printTime()
    printTime(out = System.err)
    printTime(System.err)
    printTime(divisor = 1000)

    //=> 8.9 Tail recursion
    //TAIL RECURSIVE FUNCTIONS: Functions which call themselves as their last action, are called tail recursive.
    //Often, a recursive solution is more elegant and concise than a loop-based one.
    // If the solution is tail recursive, there won’t be any runtime overhead to be paid.
    //==> Recursive, but non tail recursive, function
    def boom(x: Int): Int =
      if (x == 0) throw new Exception("boom!")
      else boom(x - 1) + 1
    try {
      boom(10)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //==> Tail recursive function
    def bang(x: Int): Int =
      if (x == 0) throw new Exception("bang!")
      else bang(x - 1)
    try {
      bang(10)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    //Scala only optimizes directly recursive calls back to the same func- tion making the call.
    // If the recursion is indirect, as in the following example of two mutually recursive functions,
    // no optimization is possible:
    def isEven(x: Int): Boolean =
      if (x == 0) true else isOdd(x - 1)
    def isOdd(x: Int): Boolean =
      if (x == 0) false else isEven(x - 1)
    //You also won’t get a tail-call optimization if the final call goes to a function value:
    // val funValue = nestedFun _
    // def nestedFun(x: Int) {
    //  if (x != 0) { println(x); funValue(x - 1) }
    //}
  }

  def chapter_7_built_in_control_structures(args: Array[String]): Unit = {
    // Instead of accumulating one higher-level control structure after another
    // in the base syntax, Scala accumulates them in libraries.
    //Almost all of Scala’s control structures result in some value

    //=> 7.1 If expressions
    //+ Using a val is the functional style, and it helps you in much the
    //same way as a final variable in Java.
    //+ Using a val instead of a var is that it better supports equational reasoning.
    //The introduced variable is equal to the expression that computes it,
    //assuming that expression has no side effects"default.txt"
    //Look for opportunities to use val s. They can make your code both easier
    // to read and easier to refactor.
    val filename = if (!args.isEmpty) args(0) else "default.txt"
    println("filename = " + filename)

    //=> 7.2 While loops
    //The while and do-while constructs are called “loops,” not expressions,
    // because they don’t result in an interesting value.
    // The type of the result is Unit.
    /*
    var line = ""
    do {
      line = readLine()
      println("Read: "+ line)
    } while (line != "")
    */
    def gcdLoop(x: Long, y: Long): Long = {
      var a = x
      var b = y
      while (a != 0) {
        val temp = a
        a = b % a
        b = temp
      }
      b
    }
    //Because the while loop results in no value, it is often left out of pure
    // functional languages.
    //For example, if you want to code an algorithm that
    // repeats a process until some condition changes, a while loop can express it
    // directly while the functional alternative, which likely uses recursion, may be
    // less obvious to some readers of the code.
    //In general, we recommend you challenge while loops in your code in the
    // same way you challenge var s. In fact, while loops and var s often go hand
    // in hand.
    def gcd(x: Long, y: Long): Long =
      if (y == 0) x else gcd(y, x % y)

    //The type of the result is Unit . It turns out that a value
    // (and in fact, only one value) exists whose type is Unit .
    def greet(): Unit = { println("hi") }
    println("greet() == (): " + (greet() == ()).toString)
    //One other construct that results in the unit value, which is relevant here,
    //is reassignment to var s.
    var line = ""
    println("(line = \"plop\") == (): " +  ((line = "plop") == ()).toString)


    //=> 7.3 For expressions
    //For expressions can iterate over multiple collections of different kinds,
    // can filter out elements based on arbitrary conditions, and can produce
    // new collections.

    //==> Iteration through collections
    //With the “ file <- filesHere ” syntax, which is called a GENERATOR, we
    // iterate through the elements of filesHere.
    //The GENERATING EXPRESSION on the right of <-
    // can be any type that has the method foreach with appropriate signatures
    val filesHere: Array[File] = new java.io.File("./src/main/scala/com/digitalpanda/scala/playground").listFiles
    for (file <- filesHere) println(file)
    for (i <- 1 to 4) println("1 to 4 : Iteration "+ i)
    for (i <- 1 until 4) println("1 until 4: Iteration "+ i)

    //==> Filtering
    for (
      file <- filesHere
      if file.isFile // <= First filer over the file val
      if file.getName.endsWith(".scala")  // <= Second filer over the file val
    ) println(file)

    //==> Nested Iteration
    //If you add multiple <- clauses, you will get nested “loops.
    def fileLines(file: java.io.File) =
      scala.io.Source.fromFile(file).getLines().toList
    def grep(pattern: String) =
      for {
        file <- filesHere
        if file.getName.endsWith(".scala")
        line <- fileLines(file) // <== nested loop
        if line.trim.matches(pattern)
      } println(file +": "+ line.trim)
    //grep(".*gcd.*")

    //==> Mid-stream variable bindings
    //Note that the previous code repeats the expression line.trim
    //You might want to only compute it once:
    def grepV2(pattern: String) =
      for {
        file <- filesHere
        if file.getName.endsWith(".scala")
        line <- fileLines(file)
        trimmed = line.trim   // <= compute once with a mid-stream variable
        if trimmed.matches(pattern)
      } println(file +": "+ trimmed)
    grepV2(".*gcd.*")

    //==> Producing a new collection
    // Generate a value to remember for each iteration. by prefixing the body of
    // the for expression by the keyword yield.
    def scalaFiles =
      for {
        file <- filesHere
        if file.getName.endsWith(".scala")
      } yield file
    //The type of the resulting collection is based on the kind of collections processed
    // in the iteration clauses. In this case the result is an Array[File] , because
    // filesHere is an array and the type of the yielded expression is File .
    // Syntax : for {clauses...} yield {body...}

    //=> 7.4 Exception handling with try expressions

    //==> Throwing exceptions
    /*
     It is safe to treat a thrown exception as any kind of value whatsoever.
     Any context that tries to use the return from a throw will never get to do so, and thus no harm will come.
     Technically, an exception throw has type Nothing
     In the following, one branch of an if computes a value, while the other throws an exception and computes Nothing
     */
    val n = 4
    val half = if (n % 2 == 0) n/2 else throw new RuntimeException("n must be even")
    println("after exception if n is odd")

    //==> Catching exceptions; The finally clause
    /*
      Scala does not require you to catch checked exceptions !!!
      The syntax for catch clauses was chosen for its consistency with an important part of Scala: PATTERN MATCHING
     */
    val file1: FileReader = null
    try {
      val file1 = new FileReader("input.txt")
      // Use and close file
    } catch {
      // If the exception is of neither type, the try-catch will terminate and the exception will propagate further
      case ex: FileNotFoundException => println("input.txt not found: " + ex.getLocalizedMessage)// Handle missing file
      case ex: IOException => println("error while reading input.txt: " + ex.getLocalizedMessage)// Handle other I/O error
    } finally {
      //file1.close()  // Be sure to close the file
    }
    println("after file not found exception handling")

    //==> Yielding a value
    //try-catch-finally results in a value
    //The default case is specified with an underscore (_), a WILDCARD SYMBOL
    // frequently used in Scala as a placeholder for a completely unknown value
    def urlFor(path: String) =
      try {
        new URL(path)
      } catch {
        case e: MalformedURLException =>
          new URL("http://www.scala-lang.org")
      }
    val testUrl = urlFor("HttpWrongFormat://helloworld.com")
    println(testUrl)

    //=> 7.5 Match expressions
    // In general a match expression lets you select using arbitrary patterns.
    // Match expressions result in a value.
    // Instead the break is implicit, and there is no fall through from one alternative to the next.
    val firstArg = if (args.length > 0) args(0) else "nothing which matches"
    val friend =
      firstArg match {
        case "salt" => "pepper"
        case "chips" => "salsa"
        case "eggs" => "bacon"
        case _ => "huh?"
      }
    println(friend)

    //=> 7.6 Living without break and continue
    //The simplest approach is to replace every continue by an if and ev- ery break by a boolean variable.
    // The boolean variable indicates whether the enclosing while loop should continue
    //Searching through an argument list for a string that ends with “.scala”
    /* JAVA Style
      int i = 0;                // This is Java
      boolean foundIt = false;
      while (i < args.length) {
        if (args[i].startsWith("-")) {
          i = i + 1;
          continue;
        }
        if (args[i].endsWith(".scala")) {
          foundIt = true;
          break;
        }
        i = i + 1;
      }
     */
    // Translated to SCALA
    var i = 0
    var foundIt = false
    while (i < args.length && !foundIt) {
      if (!args(i).startsWith("-")) {
        if (args(i).endsWith(".scala"))
          foundIt = true
      }
      i=i+1
    }
    //SCALA with tail recursion to substitute for looping ...
    def searchFrom(i: Int): Int =
      if (i >= args.length) -1
      else if (args(i).startsWith("-")) searchFrom(i + 1)
      else if (args(i).endsWith(".scala")) i
      else searchFrom(i + 1)
    i = searchFrom(0)

    //IF break is REALLY needed in SCALA...
    /*
      The Breaks class implements break by throwing an exception that is caught
      by an enclosing application of the breakable method.
     */
    val in = new BufferedReader(new InputStreamReader(System.in))
    breakable {
      while (true) {
        println("? (type enter to continue) ")
        break()
        if (in.readLine() == "") break
      }
    }

    //=> 7.7 Variable scope
    /*
     Scala’s scoping rules are almost identical to Java’s.
     One difference between Java and Scala exists, however,
     in that Scala allows you to define variables of the same name in nested scopes.
     In a Scala program, an inner variable is said to shadow a like-named outer variable,
     because the outer variable becomes invisible in the inner scope.
     */

    //The reason you can do this is that, conceptually, the interpreter creates a new nested scope
    // for each new statement you type in :
    /*
    scala> val a = 1
    a: Int = 1
    scala> val a = 2
    a: Int = 2
    scala> println(a)
    */

    //=> 7.8 Refactoring imperative-style code
    //Imperative style code:
    def printMultiTable() {
      var i = 1
      while (i <= 10) {
        var j = 1
        while (j <= 10) {
          val prod = (i * j).toString
          var k = prod.length
          while (k < 4) { print(" "); k += 1 }
          print(prod)
          j += 1 }
        println()
        i += 1
      }
    }
    printMultiTable()
    println()

    //Functional style code:
      //Returns a row as a sequence
    def makeRowSeq(row: Int) =
      for (col <- 1 to 10) yield {
        val prod = (row * col).toString
        val padding = " " * (4 - prod.length)
        padding + prod
      }
      // Returns a row as a string
    def makeRow(row: Int) = makeRowSeq(row).mkString
      // Returns table as a string with one row per line
    def multiTable() = {
      val tableSeq = // a sequence of row strings
        for (row <- 1 to 10)
          yield makeRow(row)
      tableSeq.mkString("\n")
    }
    println(multiTable())
  }

  def chapter_6_functional_objects(args: Array[String]): Unit = {
    //Functional objects : objects that do not have any mutable state.
    /*
    + IMMUTABLE OBJECTS are often easier to
    reason about than mutable ones, because they do not have complex state
    spaces that change over time.
    + Second, you can pass immutable objects
    around quite freely, whereas you may need to make defensive copies
    of mutable objects before passing them to other code.
    + Third, there is
    no way for two threads concurrently accessing an immutable to corrupt
    its state once it has been properly constructed, because no thread can
    change the state of an immutable.
    + Fourth, immutable objects make safe
    hash table keys. If a mutable object is mutated after it is placed into a
    HashSet , for example, that object may not be found the next time you
    look into the HashSet.

    -- The main disadvantage of immutable objects is that they sometimes
    require that a large object graph be copied where otherwise an update
    could be done in place. In some cases this can be awkward to express
    and might also cause a performance bottleneck.


    */

    //=> 6.2 Constructing a Rational
    //if a class doesn’t have a body, you don’t need to specify empty curly braces
    //The identifiers n and d in the parentheses after
    //the class name, Rational , are called CLASS PARAMETERS.
    //The Scala compiler will gather up these two class parameters and create
    // a PRIMARY CONSTRUCTOR that takes the same two parameters.
    //Class parameters can be used directly in the body of the class
    //Although class parameters n and d are in scope in the code of your methods
    //  you can only access their value on the object on which add was
    // invoked. (cannot access other instances class parameters)
    class RationalV1(n: Int, d: Int)
    new RationalV1(1, 2)

    /*Given this code, the Scala compiler would place the call to println into
      Rational ’s primary constructor*/
    class RationalV2(n: Int, d: Int) {
      println("Created "+ n +"/"+ d)
    }
    new RationalV2(1, 2)


    //=> 6.3 Reimplementing the toString method
    //OVERRIDE the default implementation by adding a method toString.
    class RationalV3(n: Int, d: Int) {
      override def toString: String = n +"/"+ d
    }


    //=> 6.4 Checking preconditions
    //REQUIRE will prevent the object from being constructed
    // by throwing an IllegalArgumentException if the argument is false.
    class RationalV4(n: Int, d: Int) {
      require(d != 0)
    }


    //=> 6.5 Adding fields
    //To access the numerator and denominator on "that",
    // you’ll need to make them into FIELDS.
    class RationalV5(n: Int, d: Int) {
      require(d != 0)
      val numer: Int = n
      val denom: Int = d
      def add(that: RationalV5): Rational =
        new Rational(
          numer * that.denom + that.numer * denom,
          denom * that.denom
        )
    }
    val oneHalf = new RationalV5(1, 2)
    val twoThirds = new RationalV5(2, 3)
    oneHalf add twoThirds


    //=> 6.6 Self references
    class RationalV6(n: Int, d: Int) {
      require(d != 0)
      val numer: Int = n
      val denom: Int = d

      def lessThan(that: RationalV6) =
        this.numer * that.denom < that.numer * this.denom

      def max(that: RationalV6) =
        if (lessThan(that)) that else this // <= "this" is a self reference
    }


    //=> 6.7 Auxiliary constructors
    //constructors other than the primary constructor are called
    //AUXILIARY CONSTRUCTORS.
    //In Scala, every auxiliary constructor must invoke another constructor of
    // the same class as its first action.
    //Every constructor invocation in Scala will end up eventually
    // calling the primary constructor of the class.
    //In a Scala class, only the primary constructor can invoke a superclass constructor
    class RationalV7(n: Int, d: Int) {
      require(d != 0)
      val numer: Int = n
      val denom: Int = d

      def this(n: Int) = this(n, 1) // <= Auxiliary constructor
    }


    //=> 6.8 Private fields and methods
    //An INITIALIZER is the code that initializes a variable,
    // for example, the “ n / g ” that initializes numer.
    //The Scala compiler will place the code for the initializers of Rational ’s
    // three fields into the primary constructor in the order in which they appear
    // in the source code
    class RationalV8(n: Int, d: Int) {
      require(d != 0)
      private val g = gcd(n.abs, d.abs)
      val numer = n / g
      val denom = d / g

      def this(n: Int) = this(n, 1)

      override def toString: String = numer +"/"+ denom

      private def gcd(a: Int, b: Int): Int =
        if (b == 0) a else gcd(b, a % b)
    }
    println("66/42 <=> " + new RationalV8(66, 42))


    //=> 6.9 Defining operators
    class RationalV9(n: Int, d: Int) {
      require(d != 0)
      private val g = gcd(n.abs, d.abs)
      val numer = n / g
      val denom = d / g
      def this(n: Int) = this(n, 1)
      def + (that: RationalV9): RationalV9 =
        new RationalV9(
          numer * that.denom + that.numer * denom,
          denom * that.denom
        )
      def * (that: RationalV9): RationalV9 =
        new RationalV9(numer * that.numer, denom * that.denom)
      override def toString:String = numer +"/"+ denom
      private def gcd(a: Int, b: Int): Int =
        if (b == 0) a else gcd(b, a % b)

      override def equals(that: Any): Boolean = that match {
        case x: RationalV9 => x.denom == denom && x.numer == numer
        case _ => false
      }

    }
    val x = new RationalV9(1, 2)
    val y = new RationalV9(2, 3)
    println( x + " + " + y + " = " + (x + y).toString)
    assert(x + x * y != (x + x) * y)
    assert(x + x * y == x + (x * y))


    //=> 6.10 Identifiers in Scala
    //An ALPHANUMERIC IDENTIFIER starts with a letter or underscore, which can
    // be followed by further letters, digits, or underscores. The ‘$’ character also
    // counts as a letter, however it is reserved for identifiers generated by the Scala
    // compiler
    //Scala follows Java’s convention of using camel-case 5 identifiers, such as
    // toString and HashSe
    //Avoid using underscores in identifiers  as they have many other
    // non-identifier uses in Scala code.
    //Camel-case names of fields, method parameters, local variables, and
    // functions should start with lower case letter, for example: length , flatMap ,
    // and s .
    //Camel-case names of classes and traits should start with an upper case
    // letter.
    //In Scala, the convention is merely that the first character for a constant
    //  should be upper case.

    //An OPERATOR IDENTIFIER consists of one or more operator characters.
    // Operator characters are printable ASCII characters such as + , : , ? , ~ or #
    // operator identifier examples: + ++ ::: <?> :->
    //The Scala compiler will internally “mangle” operator identifiers to turn
    // them into legal Java identifiers with embedded $ characters. For instance, the
    // identifier :-> would be represented internally as $colon$minus$greater

    //A MIXED IDENTIFIER consists of an alphanumeric identifier, which is
    // followed by an underscore and an operator identifier.
    // For example, unary_+ used as a method name defines a unary + operator.

    //A LITERAL IDENTIFIER is an arbitrary string enclosed in back ticks ( ` . . . ` ).
    // some examples of literal identifiers are: `x` `<clinit>` `yield`
    // This works even if the name contained in the back ticks would be a Scala
    //  reserved word => access Java methods with scala reserved words...


    //=> 6.11 Method overloading
    //Each of the *, +, -, / METHOD names is OVERLOADED, because each name is
    // now being used by multiple methods
    class RationalV10(n: Int, d: Int) {
      require(d != 0)
      private val g = gcd(n.abs, d.abs)
      val numer = n / g
      val denom = d / g
      def this(n: Int) = this(n, 1)
      def + (that: RationalV10): RationalV10 =
        new RationalV10(
          numer * that.denom + that.numer * denom,
          denom * that.denom
        )
      def + (i: Int): RationalV10 =
        new RationalV10(numer + i * denom, denom)
      def - (that: RationalV10): RationalV10 =
        new RationalV10(
          numer * that.denom - that.numer * denom,
          denom * that.denom
        )
      def - (i: Int): RationalV10 =
        new RationalV10(numer - i * denom, denom)
      def * (that: RationalV10): RationalV10 =
        new RationalV10(numer * that.numer, denom * that.denom)
      def * (i: Int): RationalV10 =
        new RationalV10(numer * i, denom)
      def / (that: RationalV10): RationalV10 =
        new RationalV10(numer * that.denom, denom * that.numer)
      def / (i: Int): RationalV10 =
        new RationalV10(numer, denom * i)
      override def toString = numer +"/"+ denom
      private def gcd(a: Int, b: Int): Int =
        if (b == 0) a else gcd(b, a % b)
    }
    val r = new RationalV10(2, 3)
    println("r = " + r.toString)
    println("r * r = " + (r * r).toString)
    println("r * 2 = " + (r * 2).toString)


    //=> 6.12 Implicit conversions
    //Implicit conversion that automatically converts integers to rational
    // numbers when needed.
    implicit def intToRational(x: Int): RationalV10 = new RationalV10(x)
    println("2 * r = " + (2 * r).toString)

    //If used unartfully, both operator methods and implicit conversions can
    //give rise to client code that is hard to read and understand
    //The goal you should keep in mind as you design libraries is not merely
    //enabling concise client code, but readable, understandable client code
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
    //POSTFIX operator:
    //Postfix operators are methods that take no arguments, when they are invoked without a dot or parentheses.
    // In Scala, you can leave off empty parentheses on method calls.
    val s = "Hello, world!"
    println(s)
    println(s.toLowerCase)
    println(s toLowerCase)


    //=> 5.7 Object equality
    //to compare two OBJECTS (!) for equality, you can use either == , or its inverse !=
    println(List(1, 2, 3) == List(1, 2, 3))
    println(1 == 1.0)
    println(null == List(1, 2, 3))
    println(null == null)
    //This kind of comparison will yield true on different objects, so long as
    //their contents are the same and their equals method is written to be based on
    //contents
    //Scala provides a facility for comparing reference equality, as well, under the name eq .
    //However, eq and its opposite, ne , only apply to objects that directly map to Java objects.


    //=> 5.8 Operator precedence and associativity
    // No operators, only methods. Scala decides precedence based on the first
    // character of the methods used in operator notation.
    /*
    Table 5.3 · Operator precedence
      (all other special characters)*/
    //    */%
    /*    +-
          :
          =!
          <>
          &
          ˆ
          |
          (all letters)
          (all assignment operators)
    */
    /* The one exception to the precedence rule, alluded to above, concerns
       assignment operators, which end in an equals character. If an operator ends
       in an equals character (=), and the operator is not one of the comparison
       operators <=, >=, ==, or !=, then the precedence of the operator is the same
       as that of simple assignment (=). That is, it is lower than the precedence of
       any other operator
       x *= y + 1
       means the same as:
       x *= (y + 1)
     */
    //Associativity rule :
    //a ::: b ::: c is treated as a ::: (b ::: c).
    //But a * b * c, by contrast, is treated as (a * b) * c.


    //=> 5.9 Rich wrappers
    /*These methods are available via implicit conversions, a technique that will
      be described in detail in Chapter 21. All you need to know for now is that for
      each basic type described in this chapter, there is also a “RICH WRAPPER” that
      provides several additional methods. */
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

    //The last line of a function is the returned result, if the return is unit,
    // the last line is cast to unit and thus forgotten
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


    //=> 4.4 A Scala application (useless because too limited)
    // See Summer object


    //=> 4.5 Application trait (do not use)
    // See FallWinterSpringSummer object
  }


  class ChecksumAccumulator {
    // class definition goes here.. Inside a class definition, you place fields and methods, which are collectively called members.

    //Field is an instance variable, default access level is public
    private var sum = 0

    //Method parameters are val by default (final in java)
    def add(b: Byte) { sum += b } // Procedure like braces for unit function => no "=" symbol required
    def checksum(): Int = ~ (sum & 0xFF) + 1
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

    //==> Functional (uses val, immutables,..)
    // declarative -> what to do, not how, function calls with no side effect
    //    (If Unit then probable side effect, easy to test when no side effects just check return value)
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
}