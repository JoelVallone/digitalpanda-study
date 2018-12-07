package com.digitalpanda.scala.playground

import java.util
import java.util.EmptyStackException

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class BehaviourDrivenTest extends FlatSpec with Matchers {
  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new mutable.Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "throw EmptyStackException if an empty stack is popped" in {
    val emptyStack = new util.Stack[Int]
    a [EmptyStackException] should be thrownBy {
      emptyStack.pop()
    }
  }
}