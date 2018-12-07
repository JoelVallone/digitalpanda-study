package com.digitalpanda.scala.playground

import org.scalatest.{FeatureSpec, GivenWhenThen}

class SpecDrivenTest  extends FeatureSpec with GivenWhenThen {
  feature("TV power button") {
    scenario("User presses power button when TV is off") {
      Given("a TV set that is switched off")
      When("The power button is pressed")
      Then("the TV should switch on")
      pending
    }
  }
}
