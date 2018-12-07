package com.digitalpanda.scala.playground

import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks
import org.scalatest.MustMatchers._

class PropertyBasedTest  extends WordSpec with PropertyChecks {
  "math" must {
    "adding positive a value to a positive number remains positive" in {
      forAll { (w: Int) =>
        whenever(w > 0) {
          (w + 1) > 0
        }
      }
    }
  }
}
