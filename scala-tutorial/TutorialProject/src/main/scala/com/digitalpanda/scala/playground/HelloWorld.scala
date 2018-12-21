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
    chapterSeparator(chapter_11_scala_hierarchy,11)(args)
    chapterSeparator(chapter_12_traits,12)(args)
    chapterSeparator(chapter_13_packages_and_imports,13)(args)
    chapterSeparator(chapter_14_assertions_and_unit_testing,14)(args)
    chapterSeparator(chapter_15_case_classes_and_pattern_matching,15)(args)
    chapterSeparator(chapter_16_working_with_lists,16)(args)
  }

  def chapter_16_working_with_lists(args: Array[String]): Unit = {
    //Lists are probably the most commonly used data structure in Scala programs.

    //=> 16.1 List literals
    val diag3 =
      List(
        List(1, 0, 0),
        List(0, 1, 0),
        List(0, 0, 1)
      )
    val empty = List()
    //Lists are quite similar to arrays, but there are two important differences:
    //  - First, lists are immutable. That is, elements of a list cannot be changed by assignment.
    //  - Second, lists have a recursive structure

    //=> 16.2 The List type
    //Like arrays, lists are homogeneous: the elements of a list all have the same type.
    val diag31: List[List[Int]] =
    List(
      List(1, 0, 0),
      List(0, 1, 0),
      List(0, 0, 1)
    )
    val empty1: List[Nothing] = List() // <== Nothing is the bottom type in Scala’s class hierarchy
    //The list type in Scala is COVARIANT. This means that
    // for each pair of types S and T, if S is a subtype of T, then List[S] is a subtype of List[T].

    //=> 16.3 Constructing lists
    val diag32 =
      (
        (1 :: (0 :: (0 :: Nil))) ::
        (0 :: (1 :: (0 :: Nil))) ::
        (0 :: (0 :: (1 :: Nil))) :: Nil
      )
    val empty2 = Nil
    //Because it ends in a colon, the :: operation associates to the right
    var nums = List(1,2,3)
    nums = 1::(2::(3::(4::Nil)))
    nums = 1::2::3::4::Nil

    //=> 16.4 Basic operations on lists
    //All operations on lists can be expressed in terms of the following three:
    //  - head returns the first element of a list
    //  - tail returns a list consisting of all elements except the first
    //  - isEmpty returns true if the list is empty
    //Example with insertion sort:
    def isort(xs: List[Int]): List[Int] =
      if (xs.isEmpty) Nil
      else insert(xs.head, isort(xs.tail))
    def insert(x: Int, xs: List[Int]): List[Int] =
      if (xs.isEmpty || x <= xs.head) x :: xs
      else xs.head :: insert(x, xs.tail) // <== xs is sorted
    print("isort(0::33::2::1::101::Nil) : " + isort(0::33::2::1::101::Nil))

  }

  def chapter_15_case_classes_and_pattern_matching(args: Array[String]): Unit = {
    //This chapter introduces case classes and pattern matching, twin constructs that support you when writing regular,
    // non-encapsulated data structures. These two constructs are particularly helpful for tree-like recursive data.

    //=> 15.1 A simple example
    //Let’s say you need to write a library that manipulates arithmetic expressions
    sealed abstract class Expr
    case class Var(name: String) extends Expr
    case class Number(num: Double) extends Expr
    case class UnOp(operator: String, arg: Expr) extends Expr
    case class BinOp(operator: String, left: Expr, right: Expr) extends Expr

    //==> Case classes
    //Using the "case" modifier for a class makes the Scala compiler add some syntactic conveniences to your class:
    // - First, it adds a factory method with the name of the class => no need of "new" operator:
    val v = Var("x")
    val op = BinOp("+", Number(1), v)
    // - The second syntactic convenience is that all arguments in the parameter list of a case class implicitly get
    //    a val prefix, so they are maintained as fields.
    println("op : " + op)
    println("op.left : " + op.left)
    // - Third, the compiler adds “natural” implementations of methods toString, hashCode, and equals to your class.
    //    They will print, hash, and compare a whole tree consisting of the class and (recursively) all its arguments.
    //    Since == in Scala always delegates to equals, this means that
    //    elements of case classes are always compared structurally
    println("op.right == Var(\"x\") : " + (op.right == Var("x")))
    // - Fourth, the compiler adds a copy method to your class for making modified (deep) copies.
    println("op.copy(operator = \"-\") : " + op.copy(operator = "-"))
    // - Finally, the biggest advantage of case classes is that they support pattern matching.

    //==> Pattern matching
    // A match expression is evaluated by trying each of the patterns in the order they are written.
    //  The first pattern that matches is selected, and the part following the arrow is selected and executed
    def simplifyTop(expr: Expr): Expr = expr match {
      case UnOp("-", UnOp("-", e)) => e // Double negation
      case BinOp("+", e, Number(0)) => e // Adding zero
      case BinOp("*", e, Number(1)) => e // Multiplying by one
      case _ => expr
    }

    println("simplifyTop(UnOp(\"-\", UnOp(\"-\", Var(\"x\")))) : " + simplifyTop(UnOp("-", UnOp("-", Var("x")))))
    //The right-hand side of simplifyTop consists of a match expression.
    // A PATTERN MATCH includes a sequence of alternatives, each starting with the keyword case.
    // Each ALTERNATIVE includes a pattern and one or more expression, which will be evaluated if the pattern matches.
    //    selector match { alternatives }
    // An arrow symbol => separates the pattern from the expressions.
    //A CONSTRUCTOR PATTERN looks like UnOp("-", e). This pattern matches all values of type UnOp whose first
    // argument matches "-" and whose sec- ond argument matches e. Note that the arguments to
    // the constructor are themselves patterns.

    //==> match compered to switch
    //Match expressions can be seen as a generalization of Java-style switches.
    //There are three differences to keep in mind, however:
    // - First, match is an expression in Scala, i.e., it always results in a value !!!
    // - Second, Scala’s alternative expressions never “fall through” into the next case.
    // - Third, if none of the patterns match, an exception named MatchError is thrown.

    //=> 15.2 Kind of patterns
    //==> Wildcard patterns
    // The wildcard pattern (_) matches any object whatsoever.
    // It can be used as a catch-all alternative
    // It can also be used to ignore parts of an object that you do not care about.
    def wildcard(expr: Expr): Unit = expr match {
      case BinOp(_, _, _) => println(expr + " is a binary operation")
      case _ => println("It's something else")
    }

    //==> Constant patterns
    //A constant pattern matches only itself. Any literal may be used as a constant:
    def describe(x: Any): String = x match {
      case 5 => "five"
      case true => "truth"
      case "hello" => "hi!"
      case Nil => "the empty list"
      case _ => "something else"
    }

    println("describe(5) : " + describe(5))
    println("describe(true) : " + describe(true))
    println("describe(\"hello\") : " + describe("hello"))
    println("describe(Nil) : " + describe(Nil))
    println("describe(List(1,2,3)) : " + describe(List(1, 2, 3)))

    //==> Variable patterns
    //A variable pattern matches any object, just like a wildcard.
    // Unlike a wildcard, Scala binds the variable to whatever the object is. (see "e" in simplifyTop())

    //==> Variable or Constant ?
    //Scala uses a simple lexical rule for disambiguation:
    // a simple name starting with a lowercase letter is taken to be a pattern variable;
    //If you need to, you can still use a lowercase name for a pattern constant:
    //  => For instance, `pi` would again be interpreted as a constant (like "Pi" ).

    //==> Constructor patterns
    //Ex: “BinOp("+", e, Number(0))”
    //Assuming the name designates a case class, such a pattern means to first check that the object is a member of the
    // named case class "BinOp", and then to check that the constructor parameters of the object match
    // the extra patterns supplied.
    //Scala patterns support deep matches. Such patterns not only check the top-level object supplied,
    // but also check the contents of the object against further patterns.

    //==> Sequence patterns
    //You can match against sequence types like List or Array just like you match against case classes
    def find0inList(e: Seq[Int]): String = e match {
      case List(_, 0, _*) => "found 0"
      case _ => "did not find 0"
    }

    println("find0inList(2::1::Nil) : " + find0inList(2 :: 1 :: Nil))
    println("find0inList(2::0::1::Nil) : " + find0inList(2 :: 0 :: 1 :: Nil))
    println("find0inList(2::0::Nil) : " + find0inList(2 :: 0 :: Nil))
    println("find0inList(33::1::2::0::Nil) : " + find0inList(33 :: 1 :: 2 :: 0 :: Nil))
    //If you want to match against a sequence without specifying how long it can be,
    // you can specify _* as the last element of the pattern.

    //==> Tuples pattern
    def tupleDemo(expr: Any): String = expr match {
      case (a, b, c) => "matched " + a + b + c
      case _ => ""
    }
    println("tupleDemo((\"a \", 3, \"-tuple\")) : " + tupleDemo(("a ", 3, "-tuple")))

    //==> Typed patterns
    //You can use a typed pattern as a convenient replacement for type tests and type casts
    def generalSize(x: Any) = x match {
      case s: String => s.length
      case m: Map[_, _] => m.size
      case _ => -1
    }
    println("generalSize(\"abc\") : " + generalSize("abc"))
    println("generalSize(Map(1 -> 'a', 2 -> 'b'))) : " + generalSize(Map(1 -> 'a', 2 -> 'b')))
    println("generalSize(math.Pi) : " + generalSize(math.Pi))
    //The pattern “s: String” is a typed pattern; it matches every (non-null) instance of String
    //An equivalent but more long-winded way that achieves the effect of a match against a typed pattern
    // employs a type test followed by a type cast.
    def notSoGeneralSize(x: Any) = if (x.isInstanceOf[String])  x.asInstanceOf[String].length else -1
    println("notSoGeneralSize(\"abc\") : " + notSoGeneralSize("abc"))
    println("notSoGeneralSize(1::Nil) : " + notSoGeneralSize(1::Nil))

    //==> Type erasure
    def isIntIntMap(x: Any) = x match {
      case m: Map[Int, Int] => true
      case _ => false
    }
    //Scala uses the erasure model of generics, just like Java does.
    // This means that no information about type arguments is maintained at runtime.
    //Consequently, there is no way to determine at runtime whether a given Map object has been created with
    // two Int arguments, rather than with arguments of dif- ferent types
    println("isIntIntMap(Map(1 -> 1)) : " + isIntIntMap(Map(1 -> 1)))
    println("isIntIntMap(Map(\"abc\" -> \"abc\") (type erasure...): " + isIntIntMap(Map("abc" -> "abc")))
    //The only exception to the erasure rule is arrays, because they are handled specially in Java as well as in Scala.
    // The element type of an array is stored with the array value, so you can pattern match on it.
    def isStringArray(x: Any) = x match {
      case a: Array[String] => "yes"
      case _ => "no"
    }
    println("isStringArray(Array(\"abc\")) : " + isStringArray(Array("abc")))
    println("isStringArray(Array(1, 2, 3)) (special case, no erasure with arrays) : " + isStringArray(Array(1, 2, 3)))

    //==> Variable binding
    //Variable binding pattern : You simply write the variable name, an at sign (@), and then the pattern.
    //Perform the pattern match as normal, and if the pattern succeeds, set the variable to
    // the matched object just as with a simple variable pattern.
    def extractFirstArgumentIfUnOpAbs(expr : Expr): Expr = expr match {
      case UnOp("abs", e @ UnOp("abs", _)) => e
      case _ => expr
    }
    println("extractFirstArgumentIfUnOpAbs(UnOp(\"abs\", UnOp(\"abs\", Var(\"plop\")))) : "
      + extractFirstArgumentIfUnOpAbs(UnOp("abs", UnOp("abs", Var("plop")))))

    //==> Pattern guards
    //A pattern guard comes after a pattern and starts with an if. The guard can be an arbitrary boolean expression,
    // which typically refers to variables in the pattern.
    // If a pattern guard is present, the match succeeds only if the guard evaluates to true.
    //Scala restricts patterns to be linear: a pattern variable may only appear once in a pattern
    /* => The following does not work:
      def simplifyAdd(e: Expr) = e match {
        case BinOp("+", x, x) => BinOp("*", x, Number(2))
        case _ => e
      }
    */
    //However, you can re-formulate the match with a pattern guard.
    def simplifyAdd(e: Expr) = e match {
      case BinOp("+", x, y) if x == y =>
        BinOp("*", x, Number(2))
      case _ => e
    }


    //=> 15.4 Pattern overlaps
    //Patterns are tried in the order in which they are written.


    //=> 15.5 Sealed classes
    //Sometimes you can do this by adding a default case at the end of the match,
    // but that only applies if there is a sensible default behavior.
    // What do you do if there is no default?
    //You can enlist the help of the Scala compiler in detecting missing combinations of patterns in a match expression
    //In general, this is impossible in Scala, because new case classes can be defined at any time and in arbitrary compilation units.
    //! Make the superclass of your case classes sealed (use the "sealed" class modifier).
    // A sealed class cannot have any new subclasses added except the ones in the same file
    def describeUncomplete(e: Expr): String = e match {
      case Number(_) => "a number"
      case Var(_)    => "a variable"
    }
    /*
    Produces compiler warning :
        warning: match is not exhaustive!
        missing combination           UnOp
        missing combination          BinOp
     */

    //Disable compiler warning for sealed case class with @unchecked annotation
    // If a match’s selector expression carries this annotation, exhaustivity checking for the patterns
    // that follow will be suppressed.
    def describeUnchecked(e: Expr): String = (e: @unchecked) match {
      case Number(_) => "a number"
      case Var(_)    => "a variable"
    }


    //=> 15.6 The Option type
    //Scala has a standard type named Option for optional values.
    // Such a value can be of two forms. It can be of the form Some(x) where x is the actual value.
    // Or it can be the None object, which represents a missing value.
    //Optional values are produced by some of the standard operations on Scala’s collections
    val capitals = Map("France" -> "Paris", "Japan" -> "Tokyo")
    println("capitals get \"France\" : " + (capitals get "France"))
    println("capitals get \"North Pole\" : " + (capitals get "North Pole"))
    // !!! The most common way to take optional values apart is through a pattern match:
    def show(x: Option[String]) = x match {
      case Some(s) => s
      case None => "?"
    }
    println("show(capitals get \"Japan\") : " + show(capitals get "Japan"))
    println("show(capitals get \"North Pole\") : " + show(capitals get "North Pole"))
    //The Option type is used frequently in Scala programs.
    // Compare this to the dominant idiom in Java of using null to indicate no value.
    //This approach to optional values has several advantages over Java’s:
    // - First, it is far more obvious to readers of code that a variable whose type is
    //    Option[String] is an optional String than a variable of type String, which may sometimes be null.
    // - But most importantly, that programming error described earlier of using a variable that may be null without
    //    first checking it for null becomes in Scala a type error.
    //    If a variable is of type Option[String] and you try to use it as a String, your Scala program will not compile.


    //=> 15.7 Patterns everywhere
    //Patterns are allowed in many parts of Scala, not just in standalone match expressions !!!

    //==> Patterns in variable definitions
    val myTuple = (123, "abc")
    val (number, string) = myTuple

    val exp = BinOp("*", Number(5), Number(1))
    val BinOp(operator, left, right) = exp

    //==> Case sequences as partial functions
    //A case sequence is a function literal, only more general :
    val withDefault: Option[Int] => Int = {
      case Some(x) => x
      case None => 0
    }
    println("withDefault(Some(10)) : " + withDefault(Some(10)))
    println("withDefault(None) : " + withDefault(None))
    //In general, you should try to work with complete functions whenever possible,
    // because using partial functions allows for runtime errors that the compiler cannot help you with.
    // partial functions ar functions which do not handle all possible inputs (i.e. not all cases for a case class)
    // If you want to check whether a partial function is defined,
    // you must first tell the compiler that you know you are working with partial functions:
    val second: PartialFunction[List[Int],Int] = {
      case x :: y :: _ => y
    }
    //The compiler will then generate isDefinedAt(): Boolean to check if the partial function is defined for the input value.

    //==> Patterns in for expressions
    //Example:
    for ((country, city) <- capitals)
      println("The capital of "+ country +" is "+ city)

    //Generated values that do not match the pattern are discarded:
    val results = List(Some("apple"), None, Some("orange"))
    for (Some(fruit) <- results) println(fruit)


    //=> 15.8 A larger example
    //Look at the book for explanations
    import Element.elem
    class ExprFormatter {

      // Contains operators in groups of increasing precedence
      private val opGroups =
        Array(
          Set("|", "||"),
          Set("&", "&&"),
          Set("ˆ"),
          Set("==", "!="),
          Set("<", "<=", ">", ">="),
          Set("+", "-"),
          Set("*", "%") )

      // A mapping from operators to their precedence
      private val precedence = {
        val assocs =
          for {
            i <- 0 until opGroups.length
            op <- opGroups(i)
          } yield op -> i
        assocs.toMap
      }
      private val unaryPrecedence = opGroups.length
      private val fractionPrecedence = -1

      private def format(e: Expr, enclPrec: Int): Element =
        e match {
          case Var(name) =>
            elem(name)
          case Number(num) =>
            def stripDot(s: String) =
              if (s endsWith ".0") s.substring(0, s.length - 2)
              else s
            elem(stripDot(num.toString))
          case UnOp(op, arg) =>
            elem(op) beside format(arg, unaryPrecedence)
          case BinOp("/", left, right) =>
            val top = format(left, fractionPrecedence)
            val bot = format(right, fractionPrecedence)
            val line = elem('-', top.width max bot.width, 1)
            val frac = top above line above bot
            if (enclPrec != fractionPrecedence) frac
            else elem(" ") beside frac beside elem(" ")
          case BinOp(op, left, right) =>
            val opPrec = precedence(op)
            val l = format(left, opPrec)
            val r = format(right, opPrec + 1)
            val oper = l beside elem(" "+ op +" ") beside r
            if (enclPrec <= opPrec) oper
            else elem("(") beside oper beside elem(")")
        }
      def format(e: Expr): Element = format(e, 0)
    }
    val f = new ExprFormatter
    val e1 = BinOp("*", BinOp("/", Number(1), Number(2)), BinOp("+", Var("x"), Number(1)))
    val e2 = BinOp("+", BinOp("/", Var("x"), Number(2)), BinOp("/", Number(1.5), Var("x")))
    val e3 = BinOp("/", e1, e2)
    def showExpression(e: Expr) = println(f.format(e) + "\n\n")
    for (e <- Array(e1, e2, e3)) showExpression(e)

  }


  def chapter_14_assertions_and_unit_testing(args: Array[String]): Unit = {
    //=> 14.1 Assertions
    assert( 1 == 1, "This cannot break !")
    //Assertions (and ensuring checks) can be enabled and disabled using the JVM’s -ea and -da command-line flags.
    // When enabled, each assertion serves as a little test that uses the actual data encountered as the software runs.

    { println("hello"); "hello" } ensuring( "hello" == _, " input is not equal to \"hello\"")


    //=> 14.2 Unit testing in scala
    // Look at HelloWorldTest.scala

    //=> 14.3 Informative failure reports
    // Look at HelloWorldTest.scala

    //=> 14.4 Tests as specifications
    //In the behavior-driven development (BDD) testing style, the emphasis is on writing human-readable
    // specifications of the expected behavior of code, and accompanying tests that verify the code has
    // the specified behavior. ScalaTest includes several traits —Spec, WordSpec, FlatSpec, and FeatureSpec—
    // which facilitate this style of testing.
    //Look at BehaviourDrivenTest.class
    /*
    In a FlatSpec, you write tests as specifier clauses. You start by writing a name for the subject under
      test as a string ("A Stack"), then should (or must or can),
      then a string that specifies a bit of behavior required of the subject, then in. In the curly braces following in,
      you write code that tests the specified behavior.
      In subsequent closes you can write "it" to refer to the most recently given subject.
    ScalaTest’s matchers DSL. By mixing in trait ShouldMatchers, you can write assertions that read more like
      natural language and generate more descriptive failure messages.
     */

    //=> 14.5 Property-based testing
    //ScalaCheck enables you to specify properties that the code under test must obey.
    // For each property, ScalaCheck will generate test data and run tests that check whether the property holds.


  }
  def chapter_13_packages_and_imports(args: Array[String]): Unit = {
    /*
    When working on a program, especially a large one, it is important to minimize coupling—the extent to
    which the various parts of the program rely on the other parts. Low coupling reduces the risk that a small,
    seemingly innocuous change in one part of the program will have devastating consequences in another part.

    When working on the inside of a module—its implementation—you need only coordinate with other programmers working
    on that very same module. Only when you must change the outside of a module—its interface—is it necessary to
    coordinate with developers working on other modules.
     */
    //=> 13.1 Putting code in packages
    //Scala code resides in the Java platform’s global hierarchy of packages.
    // Code with no package are put in the "unnamed" package
    //Because Scala code is part of the Java ecosystem, it is recommended to follow Java’s
    // reverse-domain-name convention for Scala packages that you release to the public
  /*
    package com.bobsrockets.navigation
    class Navigato

    OR

    package bobsrockets {
      package navigation {
        // In package bobsrockets.navigation
        class Navigator
        package tests {
          // In package bobsrockets.navigation.tests
          class NavigatorSuite
        }
      }
    }
   */
    //=> 13.2 Concise access to related code
    //If you stick to one package per file (!), then—like in Java—the only names available will be the ones defined in
    // the current package.

    //=> 13.3 Imports
    /*
    An import clause makes members of a package or object available by their names alone without needing to
    prefix them by the package or object name:
        // easy access to Fruit
        import bobsdelights.Fruit
        // easy access to all members of bobsdelights
        import bobsdelights._
        // easy access to all members of Fruits
        import bobsdelights.Fruits._
     */
    //imports in Scala can ap- pear anywhere, not just at the beginning of a compilation unit.
    /*
      package bobsdelights
      abstract class Fruit(
        val name: String,
        val color: String
      )
      object Fruits {
        object Apple extends Fruit("apple", "red")
        object Orange extends Fruit("orange", "orange")
        object Pear extends Fruit("pear", "yellowish")
        val menu = List(Apple, Orange, Pear)
      }
      def showFruit(fruit: Fruit) {
          import fruit._
          println(name +"s are "+ color) //<=== The subsequent println statement can refer to Fruit's name and color directly!
      }
    In Scala, imports:
    • may appear anywhere
    • may refer to objects (singleton or regular) in addition to packages
    • let you rename and hide some of the imported members
     */
    //One can also import packages themselves, not just their non-package members:
    import java.util.regex
    class AStarB {
      // Accesses java.util.regex.Pattern
      val pat = regex.Pattern.compile("a*b")
    }

    //This imports just classes Pattern and ASCII from package java.util.regex
    import java.util.regex.{Pattern, ASCII}
    //A renaming clause is always of the form “<original-name> => <new-name>”
    import java.sql.{Date => SDate, _} //<= rename imported class Date to Sdate and import all the rest too
    //A clause of the form “<original-name> => _” excludes <original-name> from the names that are imported.
    import java.sql.{Date => _, _} //<= import all except Date!

    //=> 13.4 Implicit imports
    //Scala adds some imports implicitly to every program as it wer manually done for :
    /*
    import java.lang._ // everything in the java.lang package
    import scala._     // everything in the scala package
    import Predef._    // everything in the Predef object
     */

    //=> 13.5 Access modifiers
    //Scala’s treatment of access modifiers roughly follows Java’s but
    // there are some important differences which are explained in this section:
    //-> Java would permit both accesses because it lets an outer class access private members of its inner classes,
    // not scala:
    class Outer {
      class Inner {
        private def f() {
          println("f")
        }
        class InnerMost {
          f() // OK }
        }
      }
      //(new Inner).f() // error: f is not accessible
    }
    //-> In Scala, a PROTECTED member is only accessible from subclasses of the class in which the member is defined.
    // In Java such accesses are also possible from other classes in the same package.
    //-> Every member not labeled private or protected is PUBLIC.
    //SCOPE PROTECTION: A modifier of the form private[X] or protected[X] means that access is private or protected
    // “up to” X[, where X designates some enclosing package,
    // OBJECT-PRIVATE: private[this] => access only from same object
    // It will not be seen from other objects of the same class.
    //COMPANION OBJECT ACCESS: A class shares all its access rights with its companion object and vice versa.
    // In particular, an object can access all private members of its companion class,
    // just as a class can access all private members of its companion object.

    //=> 13.6 Package objects
    //Any kind of definition that you can put inside a class, you can also put at the top level of a package !
    //To do so, put the definitions in a package object. Each package is allowed to have one package object.
    // Any definitions placed in a package object are considered members of the package itself.
    import com.digitalpanda.scala.playground.packageObjectHello
    packageObjectHello()
    //Package objects are compiled to class files named package.class (ex: >package scala; => scala.class)
    // that are the located in the directory of the package that they augment.
    //It’s useful to keep the same convention for source files. So you would typically put the source file of the
    // package object bobsdelights of Listing 13.14 into a file named package.scala that resides in the bobsdelights directory
  }

  def chapter_12_traits(args: Array[String]): Unit = {
    //A trait encapsulates method and field definitions, which can then be reused by mixing them into classes.
    // Unlike class inheritance, in which each class must inherit from just one superclass,
    // a class can mix in any number of traits.

    //=> 12.1 How traits work
    trait Philosophical{
      def philosophize() {
        println("I consume memory, therefore I am!")
      }
    }
    //This Philosophical trait does not declare a superclass, so like a class, it has the default superclass of AnyRef.
    //Once a trait is defined, it can be mixed in to a class using either the extends or with keywords.
    // Scala programmers “mix in” traits rather than inherit from them, because mixing in a trait has
    // important differences from the multiple inheritance found in many other languages.
    class Frog0 extends Philosophical {
      override def toString = "green"
    }
    //You can use the extends keyword to mix in a trait; in that case you implicitly inherit the trait’s superclass (plop).
    new Frog philosophize()

    class Animal
    //If you wish to mix a trait into a class that explicitly extends a superclass,
    // you use extends to indicate the superclass and with to mix in the trait.
    class Frog extends Animal with Philosophical {
      override def toString = "green"
      override def philosophize() {
        println("It ain't easy being "+ toString +"!")
      }
    }

    //Traits can, for example, declare fields and maintain state.
    // In fact, you can do anything in a trait definition that you can do in a class definition,
    // and the syntax looks exactly the same, with only two exceptions.
    // First, a trait cannot have any “class” parameters, i.e., parameters passed to the primary constructor of a class.
    // Second, super calls are statically bound, in traits, they are dynamically bound.
    //  If you write “super.toString” in a class, you know exactly which method implementation will be invoked.
    //  Rather, the implementation to invoke will be determined anew each time the trait is mixed into a concrete class.

    //=> 12.3 Thin versus rich interfaces
    // A thin interface, on the other hand, has fewer methods, and thus is easier on the implementers.
    // A rich interface has many methods, which make it convenient for the caller.
    // Clients can pick a method that exactly matches the functionality they need.
    // But with scala, you only need to implement the method once, in the trait itself,
    //  instead of needing to reimplement it for every class that mixes in the trait.
    // To enrich an interface using traits, simply define a trait with a small number of abstract methods
    // —the thin part of the trait’s interface—and a poten- tially large number of concrete methods,
    // all implemented in terms of the abstract methods. Then you can mix the enrichment trait into a class,
    // implement the thin portion of the interface, and end up with a class that has all of the rich interface available.

    //=> 12.3 Example: Rectangular objects
    //==> WITHOUT TRAITS:
    //Notice that the definitions of left, right, and width are exactly the same in the two classes.
    class Point(val x: Int, val y: Int)
    class RectangleNoTrait(val topLeft: Point, val bottomRight: Point) {
      def left = topLeft.x
      def right = bottomRight.x
      def width = right - left
      // and many more geometric methods...
    }
    abstract class ComponentNoTrait {
      def topLeft: Point
      def bottomRight: Point
      def left = topLeft.x
      def right = bottomRight.x
      def width = right - left
      // and many more geometric methods...
    }
    //==> WITH TRAITS:
    trait Rectangular {
      def topLeft: Point
      def bottomRight: Point
      def left = topLeft.x
      def right = bottomRight.x
      def width = right - left
      // and many more geometric methods...
    }
    abstract class Component extends Rectangular {
      // other methods...
    }

    class Rectangle(val topLeft: Point, val bottomRight: Point)
      extends Rectangular {
      // other methods...
    }
    val rect = new Rectangle(new Point(1, 1),new Point(10, 10))
    println("rect.left=" + rect.left)
    println("rect.right=" + rect.left)
    println("rect.width=" + rect.width)

    //=> 12.4 The Ordered trait
    //The Ordered trait then defines <, >, <=, and >= for you in terms of this one "compare" method.
    // Thus, trait Ordered allows you to enrich a class with comparison methods by implementing only one method, compare.
    class RationalOrdered(n: Int, d: Int) extends Ordered[RationalOrdered] { // <=== Ordered requires you to specify a type parameter (i.e Rational) when you mix it in.
      val numer = n
      val denom = d
      override def toString = numer +"/"+ denom

      def compare(that: RationalOrdered) =
        //It should return an integer that is
        // - zero if the objects are the same,
        // - negative if receiver is less than the argument, and
        // - positive if the receiver is greater than the argument
        (this.numer * that.denom) - (that.numer * this.denom)
        // Why not equals ?: The problem is that implementing equals in terms of compare requires checking the type of
        // the passed object, and because of type erasure, Ordered itself cannot do this test.
    }

    val half = new RationalOrdered(1, 2)
    val third = new RationalOrdered(1, 3)
    println("half < third : " + (half < third))
    println("half > third : " + (half > third))

    //12.5 Traits as stackable modifications
    //Traits let you modify the methods of a class, and they do so in a way that allows you
    // to stack those modifications with each other (sequentially call the same method defined by multiple traits).
    //Given a class that implements such a queue, you could define traits to perform modifications such as these:
    //  Doubling, Incrementing, Filtering
    //  These three traits represent modifications, because they modify the behavior of an underlying queue class
    //  rather than defining a full queue class themselves.
    abstract class IntQueue {
      def get(): Int
      def put(x: Int)
    }
    import scala.collection.mutable.ArrayBuffer
    class BasicIntQueue extends IntQueue {
      private val buf = new ArrayBuffer[Int]
      def get() = buf.remove(0)
      def put(x: Int) { buf += x }
    }
    // Doubling trait declares a superclass, IntQueue.
    //  This declaration means that the trait can only be mixed into a class that also extends IntQueue.
    //  Thus, you can mix Doubling into BasicIntQueue, but not into Rational.
    //super calls in a trait are dynamically bound, the super call in trait Doubling will work
    // so long as the trait is mixed in after another trait or class that gives a concrete definition to the put method.
    // To tell the compiler you are doing this on purpose, you must mark such methods as abstract override.
    trait Doubling extends IntQueue {
      abstract override def put(x: Int) { super.put(2 * x) }
    }
    class MyQueue extends BasicIntQueue with Doubling
    var queue: IntQueue = new MyQueue
    queue = new BasicIntQueue with Doubling; queue.put(10)
    println("val queue = new BasicIntQueue with Doubling; \nqueue.put(10); queue.get()=" + queue.get())
    trait Incrementing extends IntQueue {
      abstract override def put(x: Int) { super.put(x + 1) }
    }
    trait Filtering extends IntQueue {
      abstract override def put(x: Int) {
        if (x >= 0) super.put(x)
      }
    }
    //Given these modifications, you can now pick and choose which ones you want for a particular queue !
    //The order of mixins is significant. Roughly speaking, traits further to the right take effect first.
    //Once a trait is mixed into a class, you can alternatively call it a MIXIN.
    queue = new BasicIntQueue with Incrementing with Filtering; queue.put(-1); queue.put(0); queue.put(1)
    println("\nval queue = new BasicIntQueue with Incrementing with Filtering\nqueue.put(-1); queue.put(0); queue.put(1);" +
      "\nqueue.get()=" + queue.get()+ " queue.get()=" + queue.get())
    queue = new BasicIntQueue with Filtering with Incrementing; queue.put(-1); queue.put(0); queue.put(1)
    println("\nval queue = new BasicIntQueue with Filtering with Incrementing\nqueue.put(-1); queue.put(0); queue.put(1);" +
      "\nqueue.get()=" + queue.get() + " queue.get()=" + queue.get() + " queue.get()=" + queue.get())

    //=> 12.6 Why not multiple inheritance?
    //Trait vs multiple-inheritance : One difference is especially important: the interpretation of super.
    // The method called by a super call can be determined right where the call appears.
    // With traits, the method called is determined by a linearization of the classes and traits that are mixed into a class
    //LINERAIZATION: Scala takes the class and all of its inherited classes and traits and puts them in a single, linear order
    // Then, whenever you call super inside one of those classes, the invoked method is the next one up the chain.
    // In any linearization, a class is always linearized before all of its superclasses and mixed in traits.
    // Thus, when you write a method that calls super, that method is definitely modifying the behavior of the
    // superclasses and mixed in traits, not the other way around.
    class Animal1
    trait Furry extends Animal1
    trait HasLegs extends Animal1
    trait FourLegged extends HasLegs
    class Cat extends Animal1 with Furry with FourLegged

    //=> 12.7 To trait, or not to trait?
    //Whenever you implement a reusable collection of behavior, you will have to decide whether you want to use a
    // trait or an abstract class.
    //Guidelines:
    // - If the behavior will not be reused, then make it a concrete class
    // - If it might be reused in multiple, unrelated classes, make it a trait.
    // - If you want to inherit from it in Java code, use an abstract class.
    //   Not that a Scala trait with only abstract members translates directly to a Java interface.
    // - If you plan to distribute it in compiled form, usan abstract class
    // - If efficiency is very important, lean towards using a class.
    //   Traits get compiled to interfaces and therefore may pay a slight performance overhead
    // - If you  do not know, then start by making it as a trait as it keeps more options open

  }

  def chapter_11_scala_hierarchy(args: Array[String]): Unit = {
    // We will look at Scala’s class hierarchy as a whole.
    //In Scala, every class inherits from a common superclass named Any.
    // Because every class is a subclass of Any, the methods defined in Any are “universal” methods:
    // they may be invoked on any object.
    //Scala also defines some interesting classes at the bottom of the hierarchy,
    // Null and Nothing, which essentially act as common subclasses.
    // For example, just as Any is a superclass of every other class, Nothing is a subclass of every other class.

    //=> 11.1 Scala's class hierarchy
    //==> Any:
    // At the top of the hierarchy is class Any, which defines methods that include the following:
    //  final def ==(that: Any): Boolean
    //  final def !=(that: Any): Boolean
    //  def equals(that: Any): Boolean
    //  def ##: Int
    //  def hashCode: Int
    //  def toString: String
    // The root class Any has two subclasses: AnyVal and AnyRef.
    //==> Value classes : AnyVal
    //AnyVal is the parent class of every built-in value class in Scala.
    //There are nine such value classes: Byte, Short, Char, Int, Long, Float, Double, Boolean, and Unit.
    // The first eight of these correspond to Java’s primitive types, and their values are represented at run time
    // as Java’s primitive values.
    // The instances of these classes are all written as literals in Scala. For example, 42 is an instance of Int
    // One cannot create instances of these classes using new: value classes are all defined to be both abstract and final.
    //The other value class, Unit, corresponds roughly to Java’s void type; it is used as the result type
    // of a method that does not otherwise return an interest- ing result.
    // Unit has a single instance value, which is written (),
    //The value classes support the usual arithmetic and boolean operators as methods
    //The methods min, max, until, to, and abs are all defined in a class scala.runtime.RichInt, and
    // there is an implicit conversion from class Int to RichInt. The conversion is applied whenever
    // a method is invoked on an Int that is undefined in Int but defined in RichInt.
    // Similar “booster classes” and implicit conversions exist for the other value classes.
    //==> Reference classes: AnyRef
    //This is the base class of all reference classes in Scala.
    //AnyRef is in fact just an alias for class java.lang.Object

    //=> 11.2 How primitives are implemented
    //In fact, Scala stores integers in the same way as Java: as 32-bit words.
    //However, Scala uses the “backup” class java.lang.Integer whenever an integer needs to be seen as a (Java) object.
    //All this sounds a lot like auto-boxing in Java 5 and it is indeed quite similar. There’s one crucial difference
    /* This is Java
    boolean isEqual(Integer x, Integer y) {
      return x == y;
    }
    System.out.println(isEqual(421, 421)); // <= displays false because two different objects : not the case with Scala
    */
    //In fact, the equality operation == in Scala is designed to be transparent with respect to the type’s representation.
    // For value types, it is the natural (numeric or boolean) equality.
    // For reference types other than Java’s boxed numeric types, == is treated as an alias of
    // the equals method inherited from Object !
    //In Scala, string comparison works as it should:
    val x = "abcd".substring(2)
    val y = "abcd".substring(2)
    println("x=" + x )
    println("y=" + y )
    println("x equals y : " + (x equals y))
    println("x == y : " + (x == y))
    //AnyRef defines an additional eq method, which cannot be overridden and is implemented as reference equality
    println("x eq y : " + (x eq y)) // <= un-overridable reference equality defined in AnyRef

    //=> 11.3 Bottom types
    //At the bottom of the type hierarchy is the two classes scala.Null and scala.Nothing
    //==> Null
    //Is a subclass of every reference class (i.e., every class that itself inherits from AnyRef).
    //==> Nothing
    //Is at the very bottom of Scala’s class hierarchy; it is a sub- type of every other type
    // However, there exist no values of this type whatso-ever.
    // Why does it make sense to have a type without values? One use of Nothing is that it signals abnormal termination.
    // For instance there’s the error method in the Predef object of Scala’s standard library,
    // which is defined like this:
    def error(message: String): Nothing =
      throw new RuntimeException(message)
    //Because Nothing is a subtype of every other type, you can use methods like error in very flexible ways !
    // For instance:
    def divide(x: Int, y: Int): Int =
      if (y != 0) x / y
      else error("can't divide by zero")
  }

  def chapter_10_composition_and_inheritance(args: Array[String]): Unit = {
    //Go into the details of Scala’s support for object-oriented programming.
    // We’ll compare two fundamental relationships between classes: composition and inheritance.
    // Composition means one class holds a reference to another, using the referenced class to help it fulfill
    // its mission. Inheritance is the superclass/subclass relationship.

    //=> 10.1 A two-dimensional layout library
    //As a running example in this chapter, we’ll create a library for building and rendering two-dimensional
    // layout elements. Each element will represent a rectangle filled with text.

    //=> 10.2 Abstract classes
    //In this class, contents is declared as a method that has no implementation.
    // In other words, the method is an ABSTRACT MEMBER of class Element2.
    //Unlike Java, no abstract modifier is necessary (or allowed) on method declarations.
    //Methods that do have an implementation are called CONCRETE.
    //Class Element2 DECLARES the abstract method contents, but currently DEFINES no concrete methods.
    abstract class Element1 {
      def contents: Array[String]
    }

    //=> 10.3 defining parameter-less methods
    //The recommended convention is to use a parameter-less method whenever there are no parameters AND
    // the method accesses mutable state only by reading fields of the containing object
    // (in particular, it does not change mutable state). This convention supports the UNIFORM ACCESS PRINCIPLE,
    // which says that client code should not be affected by a decision to implement an attribute as a field or method
    //The only difference is that field accesses might be slightly faster than method invocations,
    // because the field values are pre-computed when the class is initialized.
    abstract class Element2 {
      def contents: Array[String]
      def height: Int = contents.length
      def width: Int = if (height == 0) 0 else contents(0).length
      def demo() {
        println("Element2's implementation invoked")
      }
    }
    //In principle it’s possible to leave out all empty parentheses in Scala function calls.
    // However, it is recommended to still write the empty parentheses when the invoked method represents more
    // than a property of its receiver object.

    //=> 10.4 Extending classes
    //To instantiate an element, therefore, we will need to create a subclass that extends Element2 and
    // implements the abstract contents method.
    //Such an extends clause has two effects: it makes class ArrayElement inherit all non-private members
    // from class Element2, and it makes the type ArrayElement a SUBTYPE of the type Element2.
    // Given ArrayElement extends Element2, class ArrayElement is called a SUBCLASS of class Element2.
    // Conversely, Element2 is a SUPERCLASS of ArrayElement.
    //If you leave out an extends clause, the Scala compiler implicitly assumes your class extends from scala.
    // AnyRef, which on the Java platform is the same as class java.lang.Object
    //INHERITANCE means that all members of the superclass are also members of the subclass, with two exceptions.
    // First, private members of the superclass are not inherited in a subclass.
    // Second, a member of a superclass is not inherited if a member with the same name and parameters is already
    // implemented in the subclass. In that case we say the member of the subclass OVERRIDES  the member of the superclass.
    class ArrayElement(conts: Array[String]) extends Element2 {
      def contents: Array[String] = conts // <= This relationship is called COMPOSITION because class ArrayElement is “composed” out of class Array[String]
      override def demo() {
        println("ArrayElement's implementation invoked")
      }
    }
    //SUBTYPING means that a value of the subclass can be used wherever a
    //value of the superclass is required
    val e: Element2 = new ArrayElement(Array("hello"))

    //=> 10.5 Overriding methods and fields
    //Fields and methods belong to the same namespace.
    // This makes it possible for a field to override a parameter-less method.
    //Field contents (defined with a val) in this version of ArrayElement is a perfectly good implementation
    // of the parameter-less method contents (declared with a def) in class Element2.
    class ArrayElement2(conts: Array[String]) extends Element2 {
      val contents: Array[String] = conts
    }
    //Scala has just two NAMESPACES FOR DEFINITIONS in place of Java’s four.
    // Java’s four namespaces are fields, methods, types, and packages. By contrast, Scala’s two namespaces are:
    //• values (fields, methods, packages, and singleton objects)
    //• types (class and trait names)
    //The reason Scala places fields and methods into the same namespace is precisely so you can override a
    // parameter-less method with a val, something you can’t do with Java.
    //The reason that packages share the same namespace as fields and methods in Scala is to enable you to
    // import packages in addition to just importing the names of types, and the fields and methods of singleton objects.

    //In ArrayElement2, You can avoid the "code smell" by combining the parameter and the field
    // in a single parametric field definition:
    class ArrayElement3(val contents: Array[String]) extends Element2
    //You can also prefix a class parameter with var, in which case the correspond- ing field would be re-assignable.

    //=> 10.7 Invoking superclass constructors
    //Scala requires an override modifier for all members that override a concrete member in a parent class.
    class LineElement(s: String) extends ArrayElement(Array(s)) {
      override def width = s.length

      override def height = 1

      override def demo() {
        println("LineElement's implementation invoked")
      }
    }

    //=> 10.8 Using override modifiers
    //The override rule provides useful information for the compiler that helps avoid some hard-to-catch errors
    // and makes system evolution safer
    // “accidental overrides” are the most common manifestation of what is called the “fragile base class” problem.
    // The problem is that if you add new members to base classes (which we usually call superclasses)
    // in a class hierarchy, you risk breaking client code (which happens to use the same method name before its introduction in the superclass).

    //=> 10.9 Polymorphism and dynamic binding
    //SUBTYPE POLYMORPHISM: A variable of type Element2 could refer to an object of type ArrayElement.
    //Additional Element2 sub type : UniformElement
    class UniformElement(
                          ch: Char,
                          override val width: Int,
                          override val height: Int
                        ) extends Element2 {
      private val line = ch.toString * width

      def contents = Array.fill(height)(line)
    }
    //Method invocations on variables and expressions are DYNAMICALLY BOUND.
    // This means that the actual method implementation invoked is determined at run time based on the class of
    // the object, not the type of the variable or expression.
    val e1: Element2 = new ArrayElement(Array("hello", "world"))
    val ae: ArrayElement = new LineElement("hello")
    val e2: Element2 = ae
    val e3: Element2 = new UniformElement('x', 2, 3)

    def invokeDemo(e: Element2) {
      e.demo()
    }

    invokeDemo(e1)
    invokeDemo(e2)

    //=> 10.10 Declaring final members
    //final modifier: ensures that a member cannot be overridden by sub-classes or a class cannot have sub-classes.

    //=> 10.11 Using composition and inheritance
    //Do you think clients would ever need to use a LineElement as an ArrayElement?
    //Indeed, in the previous version, LineElement had an inheritance relationship with ArrayElement,
    // from which it inherited contents. It now has a composition relationship with Array: it holds
    // a reference to an array of strings from its own contents field
    class LineElement2(s: String) extends Element2 {
      val contents = Array(s)

      override def width = s.length

      override def height = 1
    }

    //=> 10.12 implementing above, beside, and toString
    /*
    import Element2.elem
    abstract class Element2 {
      def contents: Array[String]

      def width: Int =
        if (height == 0) 0 else contents(0).length

      def height: Int = contents.length

      def above(that: Element2): Element2 =
        elem(this.contents ++ that.contents)

      def beside(that: Element2): Element2 =
        elem(
          for (
            (line1, line2) <- this.contents zip that.contents // <= name both elements line1, line2 of a pair in one pattern
          ) yield line1 + line2 // <= The for expression has a yield part and therefore yields a result.
          // The result is of the same kind as the expression iterated over, i.e., it is an array
        )

      override def toString = contents mkString "\n"
    }
  */

    //=> 10.13 Defining a factory object
    //A factory object contains methods that construct other objects.
    // Clients would then use these factory methods for object construction rather than constructing
    // the objects directly with new.
    //An advantage of this approach is that object creation can be centralized and the details of how objects are
    // represented with classes can be hidden. This hiding will both make your library simpler for
    // clients to understand, because less detail is exposed, and provide you with more opportunities to change
    // your library’s implementation later without breaking client code.
  /*
    object Element2 {
      def elem(contents: Array[String]): Element2 =
        new ArrayElement(contents)

      def elem(chr: Char, width: Int, height: Int): Element2 =
        new UniformElement(chr, width, height)

      def elem(line: String): Element2 =
        new LineElement(line)
    }
*/
    //=> 10.14 Heighten and widen

    import Element42.elem42
    object Element42 {

      private class ArrayElement42(
                                    val contents: Array[String]
                                  ) extends Element42

      private class LineElement42(s: String) extends Element42 {
        val contents = Array(s)

        override def width = s.length

        override def height = 1
      }

      private class UniformElement42(
                                      ch: Char,
                                      override val width: Int,
                                      override val height: Int
                                    ) extends Element42 {
        private val line = ch.toString * width

        def contents = Array.fill(height)(line)
      }

      def elem42(contents: Array[String]): Element42 =
        new ArrayElement42(contents)

      def elem42(chr: Char, width: Int, height: Int): Element42 =
        new UniformElement42(chr, width, height)

      def elem42(line: String): Element42 =
        new LineElement42(line)
    }
    abstract class Element42 {
      def contents: Array[String]

      def width: Int = contents(0).length

      def height: Int = contents.length

      def above(that: Element42): Element42 = {
        val this1 = this widen that.width
        val that1 = that widen this.width
        elem42(this1.contents ++ that1.contents)
      }

      def beside(that: Element42): Element42 = {
        val this1 = this heighten that.height
        val that1 = that heighten this.height
        elem42(
          for ((line1, line2) <- this1.contents zip that1.contents)
            yield line1 + line2)
      }

      def widen(w: Int): Element42 =
        if (w <= width) this
        else {
          val left = elem42(' ', (w - width) / 2, height)
          var right = elem42(' ', w - width - left.width, height)
          left beside this beside right
        }

      def heighten(h: Int): Element42 =
        if (h <= height) this
        else {
          val top = elem42(' ', width, (h - height) / 2)
          var bot = elem42(' ', width, h - height - top.height)
          top above this above bot
        }

      override def toString = contents mkString "\n"
    }

    //=> 10.15 Putting it all together
    object Spiral {
      val space = elem42(" ")
      val corner = elem42("+")

      def spiral(nEdges: Int, direction: Int): Element42 = {
        if (nEdges == 1)
          elem42("+")
        else {
          val sp = spiral(nEdges - 1, (direction + 3) % 4)

          def verticalBar = elem42('|', 1, sp.height)

          def horizontalBar = elem42('-', sp.width, 1)

          if (direction == 0)
            (corner beside horizontalBar) above (sp beside space)
          else if (direction == 1)
            (sp above space) beside (corner above verticalBar)
          else if (direction == 2)
            (space beside sp) above (horizontalBar beside corner)
          else
            (verticalBar above corner) beside (space above sp)
        }
      }
    }
    println(Spiral.spiral(6, 0))
    println(Spiral.spiral(11, 0))
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
        //The underscore is a placeholder for the file name parameter.
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
    println(twice((x) => x + 1, 5))
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
    implicit def castIntToRational(x: Int): RationalV10 = new RationalV10(x)
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