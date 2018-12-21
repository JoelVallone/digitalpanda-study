package funsets

import org.scalatest.FunSuite


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * This class is a test suite for the methods in object FunSets. To run
 * the test suite, you can either:
 *  - run the "test" command in the SBT console
 *  - right-click the file in eclipse and chose "Run As" - "JUnit Test"
 */
@RunWith(classOf[JUnitRunner])
class FunSetSuite extends FunSuite {

  /**
   * Link to the scaladoc - very clear and detailed tutorial of FunSuite
   *
   * http://doc.scalatest.org/1.9.1/index.html#org.scalatest.FunSuite
   *
   * Operators
   *  - test
   *  - ignore
   *  - pending
   */

  /**
   * Tests are written using the "test" operator and the "assert" method.
   */
  // test("string take") {
  //   val message = "hello, world"
  //   assert(message.take(5) == "hello")
  // }

  /**
   * For ScalaTest tests, there exists a special equality operator "===" that
   * can be used inside "assert". If the assertion fails, the two values will
   * be printed in the error message. Otherwise, when using "==", the test
   * error message will only say "assertion failed", without showing the values.
   *
   * Try it out! Change the values so that the assertion fails, and look at the
   * error message.
   */
  // test("adding ints") {
  //   assert(1 + 2 === 3)
  // }


  import FunSets._

  test("contains is implemented") {
    assert(contains(x => true, 100))
  }

  /**
   * When writing tests, one would often like to re-use certain values for multiple
   * tests. For instance, we would like to create an Int-set and have multiple test
   * about it.
   *
   * Instead of copy-pasting the code for creating the set into every test, we can
   * store it in the test class using a val:
   *
   *   val s1 = singletonSet(1)
   *
   * However, what happens if the method "singletonSet" has a bug and crashes? Then
   * the test methods are not even executed, because creating an instance of the
   * test class fails!
   *
   * Therefore, we put the shared values into a separate trait (traits are like
   * abstract classes), and create an instance inside each test method.
   *
   */

  trait TestSets {
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
  }

  /**
   * This test is currently disabled (by using "ignore") because the method
   * "singletonSet" is not yet implemented and the test would fail.
   *
   * Once you finish your implementation of "singletonSet", exchange the
   * function "ignore" by "test".
   */
  test("singletonSet(1) contains 1") {

    /**
     * We create a new instance of the "TestSets" trait, this gives us access
     * to the values "s1" to "s3".
     */
    new TestSets {
      /**
       * The string argument of "assert" is a message that is printed in case
       * the test fails. This helps identifying which assertion failed.
       */
      assert(contains(s1, 1), "Singleton")
      assert(!contains(s1, 2), "Singleton")
    }
  }

  test("union contains all elements of each set") {
    new TestSets {
      val s = union(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
    }
  }

  test("intersection contains same elements between two sets  s12 = intersect(s2,union(s1,s2))") {
    new TestSets {
      val s12 = union(s1, s2)
      val sIntersect = intersect(s12, s2)
      assert(contains(sIntersect, 2), "Intersect 2")
      assert(!contains(sIntersect, 1), "Intersect 1")
      assert(!contains(sIntersect, 3), "Intersect 3")
    }
  }

  test("predicate  x > 2 contains only 2 3 of 1,2,3 union") {
    new TestSets {
      def s123 = union(s3,union(s1, s2))
      def greaterThan2(x : Int): Boolean = x > 2
      def filtered2 = filter(s123, greaterThan2)
      assert(!contains(filtered2, 1), "greaterThan2 1")
      assert(!contains(filtered2, 2), "greaterThan2 2")
      assert(contains(filtered2, 3), "greaterThan2 3")
    }
  }

  test("forAll on setGreaterThan30 ") {
    def setGreaterThan30(x: Int): Boolean =  x > 30
    assert(forall(setGreaterThan30, (x: Int) => x > 0), "has all its members positive")
    assert(!forall(setGreaterThan30, (x: Int) => x < 0), "has non its members negative")
  }

  test("exist on setGreaterThan30 ") {
    def setGreaterThan30(x: Int): Boolean =  x > 30
    assert(exists(setGreaterThan30, (x: Int) => x == 31), "has at least 31")
    assert(!exists(setGreaterThan30, (x: Int) => x == 29), "does not have 29")
  }

  test("map on setGreaterThan30 ") {
    def setGreaterThan30(x: Int): Boolean =  x > 30
    assert(!exists(map(setGreaterThan30, (x:Int) => -x), (x: Int) => x == 31), "setGreaterThan30*-1 does not holds 31")
    assert(exists(map(setGreaterThan30, (x:Int) => -x), (x: Int) => x == -42), "setGreaterThan30*-1 does hold -42")
  }

}
