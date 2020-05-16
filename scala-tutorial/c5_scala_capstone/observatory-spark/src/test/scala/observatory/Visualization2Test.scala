package observatory

import observatory.Visualization2.{bilinearInterpolation, gridLocationOffset}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait Visualization2Test extends FunSuite with Checkers {


  test("'gridLocationOffset' - correctness") {
    //Given, When, Then
    assert(gridLocationOffset(Tile(255,255,8),0,0) ===  GridLocation(-85, 178))
    assert(gridLocationOffset(Tile(255,255,8),2,0) ===  GridLocation(-85, -180))
  }

  test("'bilinearInterpolation' - correctness") {
    //Given, When, Then
    assert(bilinearInterpolation(CellPoint(0.5,0.2), 91,162,210,95) ===  146.1)
  }

}
