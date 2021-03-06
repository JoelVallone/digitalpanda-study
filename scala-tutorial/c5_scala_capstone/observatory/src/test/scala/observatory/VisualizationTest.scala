package observatory


import observatory.Visualization._
import org.scalatest.prop.Checkers
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random

trait VisualizationTest extends FunSuite with Checkers with BeforeAndAfterAll{

  val colors: Iterable[(Temperature, Color)] = Iterable(
    (60.0,  Color(255,  255,  255)),
    (32.0,  Color(255,  0,    0)),
    (12.0,  Color(255,  255,  0)),
    (0.0,   Color(0,    255,  255)),
    (-15.0,	Color(0,    0,    255)),
    (-27.0,	Color(255,  0,    255)),
    (-50.0,	Color(33,   0,    107)),
    (-60.0,	Color(0,    0,    0))
  ).toArray.sortWith(_._1 < _._1)

  test("'interpolateColor' - correctness") {
    // Given
    // ... points

    // When, Then:
    // > Beyond highest
    assert(interpolateColor(colors, 70)   === Color(255,  255,  255))
    // > Bellow lowest
    assert(interpolateColor(colors, -800) === Color(0,    0,    0))
    // > Exact match positive
    assert(interpolateColor(colors, 32)   === Color(255,  0,    0))
    // > Near zero
    assert(interpolateColor(colors, 1)    === Color(21 ,  255,  234))
    // > Exact match zero
    assert(interpolateColor(colors, 0)    === Color(0,    255,  255))
    // > Interpolate positive half
    assert(interpolateColor(colors, 22)   === Color(255,  128,  0))
    // > Interpolate negative half
    assert(interpolateColor(colors, -55)  === Color(17,   0,    54))
    // > Interpolate positive
    assert(interpolateColor(colors, 31)   === Color(255,  13,   0))
    // > Interpolate negative near low
    assert(interpolateColor(colors, -59)  === Color(3,    0,    11))
    // > Interpolate negative near up
    assert(interpolateColor(colors, -51)  === Color(30,   0,    96))
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
    assert(actual === 31.0)
  }

  ignore("'predictTemperature' - visualize") {
    // Given
    val partialTemperatures = gridTemperatures(10, _ => 30.0)

    // When
    val actual = visualize(partialTemperatures, colors)
    actual.output(new java.io.File(s"test-visualize.png"))

    // Then
    assert(actual.forall((_, _, pixel) =>
      pixel.red == 255 && (pixel.green == 25 || pixel.green == 26) && pixel.blue == 0 && pixel.alpha == 255) === true)
  }

  def gridTemperatures(dropCount: Int, tempMap: Location => Temperature): IndexedSeq[(Location, Temperature)] =
    Random.shuffle(
      for {
        lat <- -89L to 90L
        lon <- -180L to 179L
      } yield {
        val location = Location(lat, lon)
        (location, tempMap(location))
      }
    ).drop(dropCount)

}
