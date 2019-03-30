package com.digitalpanda.scala.playground

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, SECONDS}

class TestWithFuturesSync extends FlatSpec with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global
    "A test with futures" should "use Await to check result synchronously" in {
      val actualFuture = Future { Thread.sleep(200); 42}
      val actual = Await.result(actualFuture, Duration(1, SECONDS))
      actual should be (42)
    }

  "A test with futures" should "could be helped with ScalaFutures trait" in {
    import org.scalatest.concurrent.ScalaFutures._
    val actualFuture = Future { Thread.sleep(10); 42}
    actualFuture.futureValue should be (42)
  }
}
