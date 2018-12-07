package com.digitalpanda.scala.playground

import org.scalatest.FunSuite

class HelloWorldTest extends FunSuite {
  test("Basic maths hold") {
    val a = 1
    val b = 2
    val res = a + b
    assert(res === 3)
    assertResult(3) {
      3
    }
    val caught = intercept[ArithmeticException] {
      1/0
    }
    assert(caught.getMessage  === "/ by zero")
  }

  test("throws IllegalArgumentException") {
    assertThrows[IllegalArgumentException]{
      throw new IllegalArgumentException("plop")
    }
  }
}



