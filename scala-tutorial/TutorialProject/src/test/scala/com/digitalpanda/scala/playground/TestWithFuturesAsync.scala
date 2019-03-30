package com.digitalpanda.scala.playground

import org.scalatest.AsyncFunSpec

import scala.concurrent.Future

//When staying in future space, the tests can be run in parallel
class TestWithFuturesAsync extends AsyncFunSpec {
  def addSoon(addends: Int* ): Future [Int] =
    Future{addends.sum}

  describe("addSoon") {
    it("will eventually compute a sum of passed Ints") {
      val futureSum = addSoon(1,2,3)
      futureSum map { sum => assert(sum == 6)}
    }
  }
}
