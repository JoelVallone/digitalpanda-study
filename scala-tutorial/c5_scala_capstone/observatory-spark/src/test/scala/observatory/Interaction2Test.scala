package observatory

import observatory.Interaction2._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait Interaction2Test extends FunSuite with Checkers {


  test("'yearBounds' - correctness") {
    // Given
    val layer = Signal(availableLayers.head)

    // When
    val bounds = yearBounds(layer)

    // Then
    assert(bounds() === availableLayers.head.bounds)
  }


  test("'yearSelection' - correctness") {
    // Given
    val layer = Signal(availableLayers.head)
    val sliderVal = Var(2014)
    val yearSelect = yearSelection(layer, sliderVal)

    // When: in bound, Then: same
    assert(yearSelect() === 2014)

    // When: out of bound, Then: nearest in bound
    sliderVal() = 2019
    assert(yearSelect() === 2015)
  }

}
