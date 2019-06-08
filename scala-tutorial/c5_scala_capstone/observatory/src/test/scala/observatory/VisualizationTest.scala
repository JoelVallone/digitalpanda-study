package observatory


import observatory.Main.sc
import observatory.Visualization._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
trait VisualizationTest extends FunSuite with Checkers with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    sc.stop()
  }

  test("'interpolateColor' - correctness") {
    // Given
    val points: Iterable[(Temperature, Color)] = Seq(
      (60,  Color(255,  255,  255)),
      (32,  Color(255,  0,    0)),
      (12,  Color(255,  255,  0)),
      (-15,	Color(0,    0,    255)),
      (0,   Color(0,    255,  255)),
      (-27,	Color(255,  0,    255)),
      (-60,	Color(0,    0,    0)),
      (-50,	Color(33,   0,    107))
    )

    // When, Then:
    // > Beyond highest
    assert(interpolateColor(points, 70)   === Color(255,  255,  255))
    // > Bellow lowest
    assert(interpolateColor(points, -800) === Color(0,    0,    0))
    // > Exact match positive
    assert(interpolateColor(points, 32)   === Color(255,  0,    0))
    // > Exact match zero
    assert(interpolateColor(points, 0)    === Color(0,    255,  255))
    // > Interpolate positive
    assert(interpolateColor(points, 22)   === Color(255,  128,  0))
    // > Interpolate negative
    assert(interpolateColor(points, -55)  === Color(17,   0,    54))
  }


  test("'predictTemperature' - correctness") {
    // Given
    val bernTrainStationTemp = (Location(46.949194, 7.438527), 30.0)
    val bernUni = Location(46.950239, 7.438368)
    val bern2kDist1UniTemp = (Location(46.950239, 7.468368), 31.0)
    val bern2kDist2UniTemp = (Location(46.950239, 7.408368), 29.0)

    val nearBeijingTemp = (Location(37.000000, 119.000000), -40.0)
    val nearBuenosAires = Location(-37.000000, -61)

    // When, Then:

    //> 1 point, Equal
    var actual = 0.0
    //var actual = predictTemperature(Seq(bernTrainStationTemp), bernTrainStationTemp._1)
    //assert(actual ===  bernTrainStationTemp._2)

    //> 1 point, Less than 1 km
    actual = predictTemperature(Seq(bernTrainStationTemp), bernUni)
    assert( actual ===  bernTrainStationTemp._2)

    //> 1 point, Antipodes
    actual = predictTemperature(Seq(nearBeijingTemp), nearBuenosAires)
    assert(actual === nearBeijingTemp._2)

    //> 2 points, 2k, 2k distance
    actual = predictTemperature(Seq(bern2kDist1UniTemp, bern2kDist2UniTemp), bernUni)
    assert(actual === 30.0)

    //> 2 points, 2k, long distance
    actual = predictTemperature(Seq(bern2kDist1UniTemp, nearBeijingTemp), bernUni)
    assert(actual === 30.999994864661232)
  }

  test("'predictTemperature' - visualize") {
    // Given
    val partialTemperatures =
      gridTemperatures(2, 30.0) ::: gridTemperatures(4, 0.0)


    val actual =
      gridTemperatures(1, 20.0)

    // When
    //visualize(partialTemperatures)

    // Then
  }

  def gridTemperatures(step: Int, temperature: Temperature): List[(Location, Temperature)] =
    (
      for {
          lat <- -180L to 179L by step
          lon <- -89 to 90 by step
      } yield {
        (Location(lat, lon), temperature * (lat / 180.0 - lon / 90.0))
      }
    ).toList

}
