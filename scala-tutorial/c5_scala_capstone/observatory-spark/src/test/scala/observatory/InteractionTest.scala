package observatory

import observatory.Interaction.tileLocation
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

trait InteractionTest extends FunSuite with Checkers {

  test("'tileLocation' - correctness") {
    assert(tileLocation(Tile(65544, 43582, 17)) === Location(51.512161249555156, 0.02197265625))
  }

  test("'subTiles' - correctness") {
    // Given
    val tile = Tile(0, 0, 0)
    val expected = List(
      //Tile(0,0,1),
      Tile(0,0,2), Tile(1,0,2), Tile(0,1,2), Tile(1,1,2),
      //Tile(1,0,1),
      Tile(2,0,2), Tile(3,0,2), Tile(2,1,2), Tile(3,1,2),
      //Tile(0,1,1),
      Tile(0,2,2), Tile(1,2,2), Tile(0,3,2), Tile(1,3,2),
      //Tile(1,1,1),
      Tile(2,2,2),Tile(3,2,2),Tile(2,3,2),Tile(3,3,2)
    )

    // When
    val actual = tile.subTiles(2)

    // Then
    assert(actual.toSet === expected.toSet)
  }

}
