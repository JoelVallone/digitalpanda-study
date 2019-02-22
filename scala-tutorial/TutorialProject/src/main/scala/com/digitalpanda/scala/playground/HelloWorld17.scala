package com.digitalpanda.scala.playground

import java.awt.event.{ActionEvent, ActionListener}
import java.io.PrintWriter
import java.util.Date

import com.digitalpanda.scala.playground.circuit.CircuitSimulation
import javax.swing.JButton

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
    chapterSeparator(chapter_20_abstract_members,20)( args )
    chapterSeparator(chapter_21_implicit_conversions_and_parameters,21)( args )
    chapterSeparator(chapter_22_implementing_lists,22)( args )
    chapterSeparator(chapter_23_for_expressions_revisited,23)( args )
  }

  def chapter_23_for_expressions_revisited(args: Array[String]): Unit = {
    //higher-order functions such as map, flatMap, and filter provide powerful constructions for dealing with lists.
    // But sometimes the level of abstraction required by these functions makes a program a bit hard to understand.
    case class Person(name: String, isMale: Boolean, children: Person*)
    val lara = Person("Lara", false)
    val bob = Person("Bob", true)
    val julie = Person("Julie", false, lara, bob)
    val persons = List(lara, bob, julie)
    val res2 = persons
      .withFilter(p => !p.isMale)
      .flatMap(p =>
        p.children.map(c => (p.name, c.name)))
    /* <=> */
    val res1 =  for (p <- persons; if !p.isMale; c <- p.children)
                yield (p.name, c.name)
    println("(mother,child) pairs: " + res1)
    //More generally, all for expressions that yield a re- sult are translated by the compiler into combinations of
    // invocations of the higher-order methods map, flatMap, and withFilter.

    //for ( SEQ ) yield expr
    // Here, SEQ is a sequence of generators, definitions, and filters, with semicolons between successive elements:
    for {
      p <- persons            // a generator
      n = p.name              // a definition
      if n startsWith "To"    // a filter
    } yield n
    //A GENERATOR is of the form:
    // pat <- expr
    //The expression expr typically returns a list, even though you will see later that this can be generalized.
    // The pattern pat gets matched one-by-one against all elements of that list. If the match succeeds,
    // the variables in the pattern get bound to the corresponding parts of the element.
    // But if the match fails, no MatchError is thrown. Instead, the element is simply discarded from the iteration.
    //A FILTER is of the form:
    //if expr
    //Here, expr is an expression of type Boolean. The filter drops from the iteration all elements for which expr returns false.
    //Every for expression starts with a generator. If there are several generators in a for expression,
    // later generators vary more rapidly than earlier ones;
    val res3 =  for (x <- List(1, 2); y <- List("one", "two"))
                yield (x, y)
    println("Multiple generators :" + res3)

    //=> 23.2 The n-queens problem
    //A particularly suitable application area of for expressions are combinatorial puzzles.
    // An example of such a puzzle is the 8-queens problem: Given a standard chess-board, place eight queens such
    // that no queen is in check from any other (a queen can check another piece if they are on the same
    // column, row, or diagonal).
    def queens(n: Int): List[List[(Int, Int)]] = {

      def placeQueens(k: Int): List[List[(Int, Int)]] =
        if (k == 0)
          List(List())
        else for {
          queens <- placeQueens(k - 1)
          column <- 1 to n
          queen = (k, column)
          if isSafe(queen, queens)
        } yield queen :: queens

      def isSafe(queen: (Int, Int), queens: List[(Int, Int)]) =
        queens forall (q => !inCheck(queen, q))
      def inCheck(q1: (Int, Int), q2: (Int, Int)) =
        q1._1 == q2._1 ||  // same row
          q1._2 == q2._2 ||  // same column
          (q1._1 - q2._1).abs == (q1._2 - q2._2).abs // on diagonal

      placeQueens(n)
    }
    println("Solutions for the 8-queens chess problem: " + queens(8))

    //=> 23.3 Querying with for expressions
    //The for notation is essentially equivalent to common operations of database query languages.
    case class Book(title: String, authors: String*)
    val books: List[Book] =
      List(
        Book(
          "Structure and Interpretation of Computer Programs",
          "Abelson, Harold", "Sussman, Gerald J."
        ), Book(
          "Principles of Compiler Design",
          "Aho, Alfred", "Ullman, Jeffrey"
        ),
        Book(
          "Programming in Modula-2",
          "Wirth, Niklaus"
        ), Book(
          "Elements of ML Programming",
          "Ullman, Jeffrey"
        ), Book(
          "The Java Language Specification", "Gosling, James",
          "Joy, Bill", "Steele, Guy", "Bracha, Gilad"
        )
      )

    //All books whose author’s last name is "Gosling"
    for (b <- books; a <- b.authors
         if a startsWith "Gosling")
      yield b.title

    //All books that have the string “Program” in their title:
    for (b <- books if (b.title indexOf "Program") >= 0)
      yield b.title

    //All authors that have written at least two books in the database
    def removeDuplicates[A](xs: List[A]): List[A] = {
      if (xs.isEmpty) xs
      else
        xs.head :: removeDuplicates(
          xs.tail filter (x => x != xs.head)
        )
    }
    removeDuplicates(
      for (b1 <- books; b2 <- books if b1 != b2;
         a1 <- b1.authors; a2 <- b2.authors if a1 == a2)
      yield a1
    )

    //=> 23.4 Translation of for expressions
    //Every for expression can be expressed in terms of the three higher-order functions map, flatMap, and withFilter.
    // This section describes the translation scheme, which is also used by the Scala compiler:
    // /* seq is an arbitrary sequence of generators */
    //  1) for (x <- expr1) yield expr2; <=> expr1.map(x => expr2);
    //  2) for (x <- expr1 if expr2) yield expr3; <=> expr1 withFilter (x => expr2) map (x => expr3)
    //  3) for (x <- expr1 if expr2; seq) yield expr3 <=> for (x <- expr1 withFilter expr2; seq) yield expr3
    //  4) for (x <- expr1; y <- expr2; seq) yield expr3 <=> expr1.flatMap(x => for (y <- expr2; seq) yield expr3)

    //All authors that have written at least two books in the database
    //Apply rule 4)
    books.flatMap( b1 =>
      for (b2 <- books if b1 != b2;
           a1 <- b1.authors; a2 <- b2.authors if a1 == a2)
        yield a1)
    //Apply rule 3)
    books.flatMap( b1 =>
      books.withFilter( b2 => b1 != b2)
      .flatMap( b2 => for (a1 <- b1.authors; a2 <- b2.authors if a1 == a2)
        yield a1))
    //Apply rule 4)
    books.flatMap( b1 =>
      books.withFilter( b2 => b1 != b2)
        .flatMap( b2 => b1.authors.flatMap( a1 => for (a2 <- b2.authors if a1 == a2)
          yield a1)))
    //Apply rule 2)
    books.flatMap( b1 =>
      books.withFilter( b2 => b1 != b2).flatMap( b2 =>
        b1.authors.flatMap( a1 =>
          b2.authors.withFilter( a2 => a1 == a2).map( _ => a1).head)))

    //==> Translating patterns in generators
    //The translation scheme becomes more complicated if the left hand side of generator is a pattern, pat,
    // other than a simple variable
    // 5) for ((x1, ..., xn) <- expr1) yield expr2 <=> expr1.map { case (x1, ..., xn) => expr2 }
    // 6) for (pat <- expr1) yield expr2 <=>
    /*    expr1 withFilter {
            case pat => true
            case _ => false
          } map {
          case pat => expr2
          }                                                            */
    //==> Translating definitions
    // 7) for (x <- expr1; y = expr2; seq) yield expr3 <=> for ((x, y) <- for (x <- expr1) yield (x, expr2); seq) yield expr3
    //So you see that expr2 is evaluated each time there is a new x value being generated.
    // This re-evaluation is necessary, because expr2 might refer to x and so needs to be re-evaluated for
    // changing values of x.
    /*
        val y = expensiveComputationNotInvolvingX
        for (x <- 1 to 1000) yield x * y
        //IS BETTER THAN
        for (x <- 1 to 1000; y = expensiveComputationNotInvolvingX)
        yield x * y                                                    */

    //==> Translating for loops
  }

  def chapter_22_implementing_lists(args: Array[String]): Unit = {
    //=> 22.1 The List class in principle
    // Lists are not “built-in” as a language construct in Scala; they are defined by
    //an abstract class List in the scala package, which comes with two sub-
    //classes for :: and Nil .
    1::2::Nil ;/* <=> */ ::(1,::(2,Nil)) ;/* <=>  */ Nil.::(2).::(1)
    println(1::2::Nil)
    println(::(1,::(2,Nil)))
    println( Nil.::(2).::(1))
    //==> List construction:
    //The list construction methods :: and ::: are special. Because they end in
    //a colon, they are bound to their right operand. That is, an operation such
    //as x :: xs is treated as the method call xs.::(x) , not x.::(xs)

    //=> 22.2 The ListBuffer class
    //The typical access pattern for a list is recursive.
    def incAll(xs: List[Int]): List[Int] = xs match {
      case List() => List()
      case x :: xs1 => x + 1 :: incAll(xs1)
    }

    //One shortcoming of this program pattern is that it is not tail recursive.
    //On today’s virtual machines this means that you cannot apply incAll to
    // lists of much more than about 30,000 to 50,000 elements...
    //One approach to avoid this problem is to use a loop..
    val xs = 1::2::3::Nil
    val buf = new ListBuffer[Int]
    for (x <- xs) buf += x + 1
    buf.toList
    //This is a very efficient way to build lists. In fact, the list buffer implemen-
    //tation is organized so that both the append operation ( += ) and the toList
    //operation take (very short) constant time.

    //=> 22.3 The List class in practice
    // (The internal implementation almost not functional at all !)
    //Most methods in the real implementation of class List avoid recursion and use loops with list buffers instead:
    /*
    final override def map[U](f: T => U): List[U] = {
          val b = new ListBuffer[U]
          var these = this
          while (!these.isEmpty) {
            b += f(these.head)
            these = these.tail
          }
      b.toList }
     */
    //A tail recursive implementation would be similarly efficient, but a general recursive implementation would
    // be slower and less scalable.
    //The .toList method takes only a small num- ber of cycles, which is independent of the length of the list.

    //=> 22.4 Functional on the outside
    //This is a typical strategy in Scala programming: trying to combine purity with efficiency by carefully
    // delimiting the effects of impure operations.
    //The design of Scala’s List and ListBuffer is quite similar to what’s done in Java’s pair of classes String and
    // StringBuffer. This is no coincidence. In both situations the designers wanted to maintain a pure
    // immutable data structure but also wanted to provide an efficient way to construct this structure incrementally.
    //Usually, :: lends itself well to recursive algorithms in the divide-and-conquer style.
    // List buffers are often used in a more traditional loop-based style.
  }

  def chapter_21_implicit_conversions_and_parameters(args: Array[String]): Unit = {
    //=> 21.1 Implicit conversions
    // Implicit conversions are often helpful for working with
    //two bodies of software that were developed without each other in mind. Each
    //library has its own way to encode a concept that is essentially the same thing.

    //As a WORD OF WARNING, implicits can make code confusing if they are
    //used too frequently. Thus, before adding a new implicit conversion, first
    //ask whether you can achieve a similar effect through other means, such as
    //inheritance, mixin composition, or method overloading. If all of these fail,
    //however, and you feel like a lot of your code is still tedious and redundant,
    //then implicits might just be able to help you out.

    //Java approach for events handling:
    val button = new JButton
    button.addActionListener(
      new ActionListener {
        def actionPerformed(event: ActionEvent) {
          println("pressed!")
        }
      }
    )

    //Use implicit conversion to express same operation but with lighter syntax:
   implicit def function2ActionListener(f: ActionEvent => Unit) =
      new ActionListener {
        def actionPerformed(event: ActionEvent) = f(event)
      }

    button.addActionListener( //<= Type mismatch if implicit conversion not used!
      (_: ActionEvent) => println("pressed!")
    )

    //=> 21.2 Rules for implicits
    // Implicit definitions are those that the compiler is allowed to insert into a
    //program in order to fix any of its type errors
    //==> Marking Rule: Only definitions marked implicit are available
    //==> Scope Rule: An inserted implicit conversion must be in scope as a single
    //    identifier, or be associated with the source or target type of the conver-
    //    sion.
    //    -> It is common for libraries to include a Preamble object including a number of
    //    useful implicit conversions. Code that uses the library can then do a single
    //    “ import Preamble._ ” to access the library’s implicit conversions.
    //    -> There’s one exception to the “single identifier” rule. The compiler will
    //    also look for implicit definitions in the companion object of the source or
    //    expected target types of the conversion.
    //    -> The Scope Rule helps with modular reasoning. If implicits took effect
    //    system-wide, then to understand a file you would have to know about
    //    every implicit introduced anywhere in the program!
    //==> One-at-a-time Rule: Only one implicit is tried.
    //==> Explicits-First Rule: Whenever code type checks as it is written, no
    //    implicits are attempted.
    //    Whenever code seems terse to the point of obscurity, you can insert
    //      conversions explicitly.
    // Where implicits are tried:
    //==> Implicit conversions to an expected type let you use one type in a
    //    context where a different type is expected.
    //==> Conversions of the receiver let you adapt the receiver of a method call
    //    , i.e., the object on which a method is invoked, if the method is not
    //    applicable on the original type.
    //    An example is "abc".exists , which is converted to stringWrapper("abc").exists
    //    because the exists method is not available on String s but is available on
    //    IndexedSeq s.
    //==> Implicit parameters, on the other hand, are usually used to
    //    provide more information to the called function about what the caller
    //    wants.
    //    Implicit parameters are especially useful with generic functions, where the
    //    called function might otherwise know nothing at all about the type of one
    //    or more arguments.

    //=> 21.3 Implicit conversion to an expected type
    // Whenever the compiler sees an X, but needs a Y, it will look for an
    // implicit function that converts X to Y.
    implicit def doubleToInt(x: Double) = x.toInt
    val i: Int = 3.5 //<= implicitly translated to>val i: Int = doubleToInt(3.5)
    //It makes much more sense to go the other way, from some more constrained type
    // to a more general one.

    //=> 21.4 Converting the receiver
    //==> Inter-operating with new types
    // One major use of receiver conversions is allowing smoother integration of
    //new with existing types. In particular, they allow you to enable client
    //programmers to use instances of existing types as if they were instances
    // of your new type.
    implicit def intToRational(x: Int) = new Rational(x, 1)
    val j = 1 +  new Rational(1,2)
    //==> Simulating new syntax
    // Whenever you see someone calling methods that appear not
    //to exist in the receiver class, they are probably using implicits. Similarly, if
    //you see a class named RichSomething , e.g., RichInt or RichBoolean , that
    //class is likely adding syntax-like methods to type Something .
    Map(1 -> "one", 2 → "two", 3 -> "three")
    // Have you wondered how the -> is supported? It’s not syntax! Instead, -> is
    //a method of the class ArrowAssoc , a class defined inside the standard Scala
    // preamble ( scala.Predef ).
    /*
    package scala
    object Predef {
      class ArrowAssoc[A](x: A) {
        def -> [B](y: B): Tuple2[A, B] = Tuple2(x, y)
      }
      implicit def any2ArrowAssoc[A](x: A): ArrowAssoc[A] = new ArrowAssoc(x)
      ...
    }
     */

    //=> 21.5 Implicit parameters
    // The remaining place the compiler inserts implicits is within argument lists.
    //The compiler will sometimes replace someCall(a) with someCall(a)(b, c, d) ,
    //or new SomeClass(a) with new SomeClass(a)(b, c, d) , thereby adding a miss-
    //ing parameter list to complete a function call. It is the entire last curried
    //parameter list that’s supplied, not just the last parameter. For this usage,
    //not only must the inserted identifiers, such as b , c , and d in (b, c, d) , be
    //marked implicit where they are defined, but also the last parameter list in
    //someCall ’s or someClass ’s definition must be marked implicit.
    class PreferredPrompt(val preference: String)
    class PreferredDrink(val preference: String)
    object Greeter {
      //Note that the implicit keyword applies to an entire parameter list, not
      //to individual parameters.
      def greet(name: String)(implicit prompt: PreferredPrompt,
                              drink: PreferredDrink) {
        println("Welcome, "+ name +". The system is ready.")
        print("But while you work, ")
        println("why not enjoy a cup of "+ drink.preference +"?")
        println(prompt.preference)
      }
    }
    object JoesPrefs {
      implicit val prompt = new PreferredPrompt("Yes, master> ")
      implicit val drink = new PreferredDrink("tea")
    }
    val bobsPrompt = new PreferredPrompt("relax> ")
    val bobsDrink = new PreferredDrink("coffee")
    Greeter.greet("Bob")(bobsPrompt, bobsDrink) // <= prompt explicitly specified
    import JoesPrefs._ // <= Statisfy implicits "Scope" rule for input parameters
    Greeter.greet("Joe") // <= Implicit parameters...
    // One thing to note about the previous examples is that we didn’t use
    //String as the type of prompt or drink , even though ultimately it was a
    //String that each of them provided through their preference fields. Be-
    //cause the compiler selects implicit parameters by matching types of parame-
    //ters against types of values in scope, implicit parameters usually have “rare”
    //or “special” enough types that accidental matches are unlikely.
    //...
    def maxListImpParm[T](elements: List[T])
                         (implicit orderer: T => Ordered[T]): T =
      elements match {
        case List() =>
          throw new IllegalArgumentException("empty list!")
        case List(x) => x
        case x :: rest =>
          val maxRest = maxListImpParm(rest)(orderer) // (orderer) may also be provided implicitly
          if (orderer(x) > maxRest) x // (orderer) may also have been called implicitly
          else maxRest
      }
    //This pattern is so common that the standard Scala library provides im-
    //plicit “orderer” methods for many common types. You could therefore use
    //this maxListImpParm method with a variety of types
    println("maxListImpParm(List(1,5,10,3)): " + maxListImpParm(List(1,5,10,3)))
    println("maxListImpParm(List(1.5, 5.2, 10.7, 3.14159)): " + maxListImpParm(List(1.5, 5.2, 10.7, 3.14159)))
    println("maxListImpParm(List(\"one\", \"two\", \"three\")): " + maxListImpParm(List("one", "two", "three")))
    //==> A style rule for implicit parameters: As a style rule, it is best to use a
    //custom named type in the types of implicit parameters. That i, use at least
    // one role-determining name within the type of an implicit parameter.

    //=>21.6 View bounds (deprecated => use implicit parameter...)
    // The previous example had an opportunity to use an implicit but did not. Note
    //that when you use implicit on a parameter, then not only will the compiler
    //try to supply that parameter with an implicit value, but the compiler will also
    //use that parameter as an available implicit in the body of the method!
    //Thus, the implicit conversion parameter does not even need a name (or to be mentioned):
    def maxList[T <% Ordered[T]](elements: List[T]): T =
    elements match {
      case List() =>
        throw new IllegalArgumentException("empty list!")
      case List(x) => x
      case x :: rest =>
        val maxRest = maxList(rest) // (orderer) is implicit
        if (x > maxRest) x // orderer(x) is implicit
        else maxRest
    }
    //You can think of “ T < % Ordered[T] ” as saying, “I can use any T , so long
    //as T can be treated as an Ordered[T] .” (as long as n implicit conversion from T to Ordered[T] exists and is in scope).
    // This is different from saying that T is an Ordered[T] , which is what
    // an upper bound, “ T <: Ordered[T] ”, would say.

    //=> 21.7 When multiple conversions apply
    // It can happen that multiple implicit conversions are in scope that would each
    //work. For the most part, Scala refuses to insert a conversion in such a case.
    // Scala 2.8 loosens this rule. If one of the available conversions is strictly
    //more specific than the others, then the compiler will choose the more specific
    //one.
    //To be more precise, one implicit conversion is more specific than another
    //if one of the following applies:
    // • The argument type of the former is a subtype of the latter’s.
    // • Both conversions are methods, and the enclosing class of the former
    //extends the enclosing class of the latter.
    //The motivation to revisit this issue and revise the rule was to improve in-
    //ter-operation between Java collections, Scala collections, and strings.$

    //=> 21.8 Debugging implicits
    // Sometimes you might wonder why the compiler did not find an implicit
    //conversion that you think should apply. In that case it helps to write the
    //conversion out explicitly. If that also gives an error message, you then know
    //why the compiler could not apply your implicit.
    //On the other hand, it’s also possible that inserting the conversion
    //explicitly will make the error go away. In that case you know that one of the
    //other rules (such as the Scope Rule) was preventing the implicit conversion
    //from being applied.
    // When you are debugging a program, it can sometimes help to see what
    //implicit conversions the compiler is inserting. The -Xprint:typer option
    //to the compiler is useful for this.
  }

  def chapter_20_abstract_members(args: Array[String]): Unit = {
    //A member of a class or trait is abstract if the member does not have a complete definition in the class.
    // Abstract members are intended to be implemented in subclasses of the class in which they are declared.

    //=> 20.1 A quick tour of abstract members
    // Besides methods, you can also declare abstract fields and even abstract types as members of classes and traits:
    trait AllAbstract {
      type T
      def transform(x: T): T
      val initial: T
      var current: T
    }

    class Concrete extends AllAbstract {
      type T = String
      def transform(x: String) = x + x
      val initial = "hi"
      var current = initial
    }

    //=> 20.2 Type members
    //The term abstract type in Scala means a type declared (with the “type” keyword) to be a member of a class or trait,
    // without specifying a definition.

    //=> 20.3 Abstract vals
    //An abstract val declaration has a form like:
    //        val initial: String
    //It gives a name and type for a val, but not its value. This value has to be provided by a
    // concrete val definition in a subclass.
    //Abstract method declarations, on the other hand, may be implemented by both concrete method definitions
    // and concrete val definitions.

    //=> 20.4 Abstract vars
    //Like an abstract val, an abstract var declares just a name and a type, but not an initial value.
    //vars declared as members of classes come equipped with getter and setter methods.
    // This holds for abstract vars as well. If you declare an abstract var named hour,
    // for example, you implicitly declare an abstract getter method, hour, and an abstract setter method,
    // hour_=. There’s no reassignable field to be defined
    trait AbstractTime {
      var hour: Int
      var minute: Int
    }
    // <=>
    trait AbstractTimeFieldLess {
      def hour: Int         // getter for ‘hour’
      def hour_=(x: Int)    // setter for ‘hour’
      def minute: Int       // getter for ‘minute’
      def minute_=(x: Int)  // setter for ‘minute’
    }

    //=> 20.5 Initializing abstract vals
    trait RationalTrait {
      val numerArg: Int
      val denomArg: Int
    }
    //A class parameter argument is evaluated before it is passed to the class constructor
    // (unless the parameter is by-name ( => )).
    class Rational(n: Int, d : Int) extends RationalTrait {
      override val numerArg: Int = n
      override val denomArg: Int = d
    }
    new Rational(1, 2)
    //An implementing val definition in a subclass, by contrast, is evaluated only after the superclass has been initialized.
    // This expression yields an instance of an anonymous class that mixes in the trait and is defined by the body.
    new RationalTrait {
      val numerArg = 1
      val denomArg = 2
    }
    //A class parameter argument is evaluated before it is passed to the class constructor (unless the parameter is by-name).
    // An implementing val definition in a subclass, by contrast, is evaluated only after the superclass has been initialized.
    //Is it possible to define a RationalTrait that can be initialized robustly, without fearing errors due to uninitialized fields?
    // => two alternative solu- tions to this problem, pre-initialized fields and lazy vals.

    //- Pre-initialized fields
    //Initialize a field of a subclass before the superclass is called... :
    new {
      val numerArg = 1
      val denomArg = 2

      //val denomArg = this.numerArg * 2 // <= does not work: If such an initializer refers to this, the reference goes to the object containing the class or object that’s being constructed (empty), not the constructed object itself
    } with RationalTrait

    //- Lazy vals
    //If you prefix a val definition with a lazy modifier, the initializing expression on the right-hand side
    // will only be evaluated the first time the val is used.
    //lazy vals are an ideal complement to functional objects, where the order of initializations does not matter,
    // as long as every- thing gets initialized eventually.
    //They are less well suited for code that’s predominantly imperative.
    object Demo {
      val x = { println(" -> initializing x"); "done" }
    }
    println("Demo ref: "); Demo
    println()
    println("Demo.x: " + Demo.x)
    object LazyDemo {
      lazy val x = { println(" -> initializing x"); "done" }
    }
    println()
    println("LazyDemo ref: ");LazyDemo
    println()
    println("LazyDemo.x: " + LazyDemo.x)

    //=> 20.6 Abstract types
    class Food
    abstract class Animal {
      type SuitableFood <: Food // <- abstract class defined by child : a Cow specifically eats Grass
      def eat(food: SuitableFood)
    }
    class Grass extends Food
    class Cow extends Animal {
      type SuitableFood = Grass
      override def eat(food: Grass) {}
    }

    //=> 20.7 Path-dependent types
    //Objects in Scala can have types as members (like a variable)!
    //A type like bessy.SuitableFood is called a path-dependent type.
    // The word “path” here means a reference to an object.
    //The term “path-dependent type” says, the type depends on the path: in general, different paths give
    // rise to different types. For instance, say you defined classes DogFood and Dog, like this:
    class DogFood extends Food
    class Dog extends Animal {
      type SuitableFood = DogFood
      override def eat(food: DogFood) {}
    }
    val bessy = new Cow
    val lassie = new Dog
    //lassie eat (new bessy.SuitableFood) // <- compile error: type mismatch
    val bootsie = new Dog
    lassie eat (new bootsie.SuitableFood) // <- Because Dog’s SuitableFood type is defined to be an alias for class DogFood,
                                          //     the SuitableFood types of two Dogs are in fact the same.
    class Outer {
      class Inner
    }
    //In Scala, the inner class is addressed using the expression Outer#Inner
    val o1 = new Outer
    val o2 = new Outer
    //Here o1.Inner and o2.Inner are two path-dependent types (and they are different types).
    // Both of these types conform to (are subtypes of) the more general type Outer#Inner,
    // which represents the Inner class with an arbitrary outer object of type Outer.
    //In Scala, as in Java, inner class instances hold a reference to an enclosing outer class instance.
    // This allows an inner class, for example, to access mem- bers of its outer class.
    // Type o1.Inner refers to the Inner class with a specific outer object (the one referenced from o1).
    new o1.Inner
    //By contrast, because the type Outer#Inner does not name any specific instance of Outer,
    // you can’t create an instance of it:
    //new Outer#Inner //<- Will not compile

    //=> 20.8 Structural sub-typing
    //When a class inherits from another, the first class is said to be a nominal subtype of the other one.
    // It’s a NOMINAL SUBTYPE because each type has a name, and the names are explicitly declared to have a subtyping
    // relationship.
    //Scala additionally supports STRUCTURAL SUBTYPEing, where you get a subtyping relationship simply because
    // two types have the same members.
    //refinement type: A type formed by supplying a base type a number of members inside curly braces.
    //A widget can draw(), and a Western cowboy can draw(), but they aren’t really substitutable.
    // You’d typically prefer to get a compilation error (by relying on nominal subtypes) if you tried to substitute a cowboy for a widget.
    class Pasture {
      var animals: List[Animal { type SuitableFood = Grass }] = Nil //<- The list type is a structural type for Animals that eats grass...
      // ...
    }
    def using[T <: { def close(): Unit }, S](obj: T)(operation: T => S) = {
      val result = operation(obj)
      obj.close() // <- the only need for the class is to define the close method !!!!
      result
    }
    using(new PrintWriter("date.txt")) { writer =>
      writer.println(new Date)
    }

    //=> 20.9 Enumerations
    object Color extends Enumeration {
      val Red, Green, Blue = Value
    }
    object Direction extends Enumeration {
      val North = Value("To the North!")
      val East = Value("To the East!")
      val South = Value("To the South!")
      val West = Value("To the West!")
    }
    println("Direction.North: " + Direction.North)
    print("Iterate: "); for (d <- Direction.values) print(d +" "); println()
    println("Direction.North.id: " + Direction.North.id)
    println("Direction(0): " + Direction(0))

    //=> 20.10 Case study: Currencies
    //See the files in the following package:
    import currencies._
    println("Japan.Yen.from(US.Dollar * 100): " + Japan.Yen.from(US.Dollar * 100))



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
    class QueueTutorial[T] private(
                                    private val leading: List[T],
                                    private val trailing: List[T]
                                  ) {

      private def mirror = if (leading.isEmpty) new QueueTutorial(trailing.reverse, Nil) else this

      def head = mirror.leading.head

      def tail = {
        val q = mirror
        new QueueTutorial(q.leading.tail, q.trailing)
      }

      def enqueue(x: T) =
        new QueueTutorial(leading, x :: trailing)
    }

    //=> 19.2 Information hiding
    //What’s needed is a way to hide this constructor from client code
    // - Private constructors and factory methods
    //In Java, you can hide a constructor by making it private.
    // In Scala, the primary constructor does not have an explicit definition;
    // it is defined implicitly by the class parameters and body.
    // Nevertheless, it is still possible to hide the primary constructor by adding
    // a private modifier in front of the class parameter list.
    //1) One possibility is to add an auxiliary constructors:
    class StrangeQueue[T] private(
                                   private val leading: List[T],
                                   private val trailing: List[T]
                                 ) {
      def this() = this(Nil, Nil)

      def this(elems: T*) = this(elems.toList, Nil)
    }
    //2) Another possibility is to add a factory method that builds a queue from such a sequence of initial elements.
    // A neat way to do this is to define an object Queue
    // that has the same name as the class being defined and contains an apply method,
    object QueueTutorial {
      // constructs a queue with initial elements ‘xs’
      def apply[T](xs: T*) = new QueueTutorial[T](xs.toList, Nil)
    }
    //By placing this object in the same source file as class Queue,
    // you make the object a companion object of the class.
    //A companion object has the same access rights as its class.
    QueueTutorial(1, 2, 3)
    QueueTutorial.apply(1, 2, 3) //<- same as QueueTutorial(1,2,3) since QueueTutorial is an object instead of a function.
    // - An alternative: private classes
    //Private constructors and private members are one way to hide the initialization and representation of a class.
    // Another, more radical way is to hide the class itself and only export a trait that reveals the public interface
    // of the class:
    trait QueueSecretive[T] {
      def head: T

      def tail: QueueSecretive[T]

      def enqueue(x: T): QueueSecretive[T]
    }

    object QueueSecretive {
      def apply[T](xs: T*): QueueSecretive[T] = new QueueImpl[T](xs.toList, Nil)

      private class QueueImpl[T](
                                  private val leading: List[T],
                                  private val trailing: List[T]) extends QueueSecretive[T] {

        def mirror =
          if (leading.isEmpty)
            new QueueImpl(trailing.reverse, Nil)
          else
            this

        def head: T = mirror.leading.head

        def tail: QueueImpl[T] = {
          val q = mirror
          new QueueImpl(q.leading.tail, q.trailing)
        }

        def enqueue(x: T) =
          new QueueImpl(leading, x :: trailing)
      }

    }

    //=> 19.3 Variance annotations
    //The variance annotations help to have more general operations as it allows subtypes to benefit from the logic targeting a reference type.
    //Queue is not a type because it takes a type parameter. As a result, you cannot create variables of type Queue.
    //Instead, trait Queue enables you to specify parameterized types, such as Queue[String].
    //Queue is also called a TYPE CONSTRUCTOR
    //You can also say that Queue is a GENERIC trait. The term “generic” means that you are defining many specific
    // types with one generically written class or trait.
    //More generally, if S is a subtype of type T, then should Queue[S] be considered a subtype of Queue[T] ?
    //If so, you could say that trait Queue is COVARIANT (or “flexible”) in its type parameter T.
    // A covariant annotation can be applied to a type parameter of a class or trait by putting a plus sign (+)
    // before the type parameter. The class or trait then subtypes co-variantly with—in the same direction as—the
    // type annotated parameter. For example, List is covariant in its type parameter,
    // so List[String] is a subtype of List[Any].
    //In Scala, however, generic types have by default NON-VARIANT (or, “rigid”) sub-typing.
    //  A Queue[String] would not be usable as a Queue[AnyRef]
    //However, you can demand covariant (flexible) subtyping of queues by changing the first line of the definition of
    // class Queue like this:> trait Queue[+T] { ... }
    // Prefixing a formal type parameter with a + indicates that subtyping is covariant (flexible) in that parameter
    //Prefix -, which indicates CONTRAVARIANT subtyping
    // The class or trait then subtypes contravariantly with— in the opposite direction as—the type annotated
    // parameter. For ex- ample, Function1 is contravariant in its first type parameter,
    // and so Function1[Any, Any] is a subtype of Function1[String, Any].
    //Whether a type parameter is covariant, contravariant, or nonvariant is called the parameter’s VARIANCE
    //The + and - symbols you can place next to type parameters are called VARIANCE ANNOTATIONS.
    //In a purely functional world, many types are naturally covariant (flexi- ble).
    // However, the situation changes once you introduce mutable data.
    /*
      //Assume mutable Cell[+T] <= would not compile because Cell is mutable and declared contravariant Cell[T] in scala lib
      val c1 = new Cell[String]("abc")
      val c2: Cell[Any] = c1
      c2.set(1)
      val s: String = c1.get
     */
    //Taken together, these four lines end up assigning the integer 1 to the string s
    //Which operation is to blame for the runtime fault? It must be the second one, which uses covariant sub-typing.

    //- Variance and arrays
    //In principle, arrays are just like cells except that they can have more than one element.
    // Nevertheless, arrays are treated as covariant in Java.
    /*
      //This is Java
      String[] a1 = { "abc" };
      Object[] a2 = a1;
      a2[0] = new Integer(17);
      String s = a1[0];
     */
    //If you try out this example, you will find that it compiles, but executing the program
    // will cause an ArrayStore exception to be thrown when a2[0] is assigned to an Integer !
    //What happens here is that Java stores the element type of the array at run- time.
    // Then, every time an array element is updated, the new element value is checked against the stored type.
    // If it is not an instance of that type, an ArrayStore exception is thrown.
    // There was a good reason. see page 432...
    //Scala treats arrays as non-variant.

    //=> 19.4 Checking variance annotations
    //As soon as a generic parameter type appears as the type of a method parameter,
    // the containing class or trait may not be covariant in that type parameter (mutable or not).

    //=> 19.5 Lower bounds
    //The previous definition of Queue[T] shown in Listing 19.4 cannot be made covariant in T because T appears as
    // a type of a parameter of the enqueue method.
    //There’s a way to get unstuck: you can generalize enqueue by making it polymorphic
    // (i.e., giving the enqueue method itself a type parameter) and using a lower bound for its type parameter.
    class Queue[+T](private val leading: List[T], // <- Covariant in T
                    private val trailing: List[T]) {
      def enqueue[U >: T](x: U) = //<- input type U has T as lower bound <=> U is required to be a supertype of T
        new Queue[U](leading, x :: trailing)
    }
    //As an example, suppose there is a class Fruit with two subclasses, Apple and Orange.
    // With the new definition of class Queue, it is possible to append an Orange to a Queue[Apple].
    // The result will be a Queue[Fruit]. This revised definition of enqueue is type correct.
    //They are a good example of TYPE-DRIVEN(constrained...) DESIGN, where the types of an interface guide
    // its detailed design and implementation.
    //Adding a lower bound makes enqueue more general and queues as a whole more usable.

    //19.6 Contra-variance
    trait OutputChannel[-T] {
      def write(x: T)
    }
    //So an output channel of AnyRefs (Object in java), is a subtype of an output channel of Strings.
    //This reasoning points to a general principle in type system design:
    // It is safe to assume that a type T is a subtype of a type U if you can substitute a value of type T
    // wherever a value of type U is required.
    // This is called the LISKOV SUBSTITUTION PRINCIPLE.
    // The principle holds if T supports the same operations as U and all of T’s operations require less and provide more => WHY !?
    //In the case of output channels, an OutputChannel[AnyRef] can be a subtype of an OutputChannel[String] because
    // the two support the same write operation, and this operation re- quires less in OutputChannel[AnyRef] than
    // in OutputChannel[String].
    // “Less” means the argument is only required to be an AnyRef in the first case,
    // whereas it is required to be a String in the second case.

    trait FunctionTutorial1[-S, +T] {
      def apply(x: S): T
    }
    //This satisfies the Liskov substitution principle, because arguments are something that’s required,
    // whereas results are something that’s provided.
    class Publication(val title: String)
    class Book(title: String) extends Publication(title)
    object Library {
      val books: Set[Book] =
        Set(
          new Book("Programming in Scala"),
          new Book("Walden")
        )

      def printBookList(info: Book => AnyRef) { // <- THE BODY OF THE printBookList METHOD WILL ONLY BE ALLOWED TO PASS A Book INTO THE FUNCTION !
        for (book <- books) println(info(book)) // <- println can work on AnyRef down to String (and even deeper). Indeed, String implements all that Anyref has and println relies on Anyref interface.
      }
    }
    object Customer { // extends Application {
      def getTitle(p: Publication): String = p.title

      Library.printBookList(getTitle)
    }

    //=> 19.7 Object private data
    //We build a new implementation of Queue, which performs at most one trailing to leading adjustment for any
    // sequence of head operations by adding some judicious side effects.
    class EfficientQueue[+T] private (
                              private[this] var leading: List[T], // <- accesses to private variables from the same object in which they are defined do not cause problems with variance.
                              private[this] var trailing: List[T]
                            ){
      private def mirror() =
        if (leading.isEmpty) {
          while (!trailing.isEmpty) {
            leading = trailing.head :: leading
            trailing = trailing.tail
          }
        }
      def head: T = {
        mirror()
        leading.head
      }
      def tail: EfficientQueue[T] = {
        mirror()
        new EfficientQueue(leading.tail, trailing)
      }
      def enqueue[U >: T](x: U) =
        new EfficientQueue[U](leading, x :: trailing)
    }

    //=> 19.8 Upper bounds
    class Person(val firstName: String, val lastName: String)
      extends Ordered[Person] {
      def compare(that: Person) = {
        val lastNameComparison =
          lastName.compareToIgnoreCase(that.lastName)
        if (lastNameComparison != 0)
          lastNameComparison
        else
          firstName.compareToIgnoreCase(that.firstName)
      }
      override def toString = firstName +" "+ lastName
    }
    val robert = new Person("Robert", "Jones")
    val sally = new Person("Sally", "Smith")
    println("robert < sally: " +  (robert < sally))
    def orderedMergeSort[T <: Ordered[T]](xs: List[T]): List[T] = { //<- T must be a trait/subtype of Ordered[T] Type, Ordered[T] is the upper bound
      def merge(xs: List[T], ys: List[T]): List[T] =
        (xs, ys) match {
          case (Nil, _) => ys
          case (_, Nil) => xs
          case (x :: xs1, y :: ys1) =>
            if (x < y) x :: merge(xs1, ys)
            else y :: merge(xs, ys1)
        }
      val n = xs.length / 2
      if (n == 0) xs
      else {
        val (ys, zs) = xs splitAt n
        merge(orderedMergeSort(ys), orderedMergeSort(zs))
      }
    }
    val people = List(
      new Person("Larry", "Wall"),
      new Person("Anders", "Hejlsberg"),
      new Person("Guido", "van Rossum"),
      new Person("Alan", "Kay"),
      new Person("Yukihiro", "Matsumoto")
    )
    val sortedPeople = orderedMergeSort(people)
    println("orderedMergeSort(people): " + orderedMergeSort(people))
    //Tt isn’t actually the most general way in Scala to design a sort function that takes advantage of the Ordered trait.
    //You couldn’t use the orderedMergeSort function to sort a list of integers, because class Int is not a subtype of Ordered[Int]
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