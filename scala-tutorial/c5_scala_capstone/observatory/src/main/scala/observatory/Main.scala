package observatory

import java.util.Calendar

import observatory.Extraction._
import observatory.Visualization._
import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  val workerCount : Int = 1

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  @transient lazy val conf: SparkConf = new SparkConf()
  .setMaster(s"local")
  .setAppName("StackOverflow")
  .set("spark.driver.bindAddress", "127.0.0.1")

  @transient lazy val sc: SparkContext = new SparkContext(conf)

  private val colors: Iterable[(Temperature, Color)] = Seq(
    (60,  Color(255,  255,  255)),
    (32,  Color(255,  0,    0)),
    (12,  Color(255,  255,  0)),
    (-15,	Color(0,    0,    255)),
    (0,   Color(0,    255,  255)),
    (-27,	Color(255,  0,    255)),
    (-60,	Color(0,    0,    0)),
    (-50,	Color(33,   0,    107))
  )

  //temperatureImageSpark(1975)

  def temperatureImageSpark(year: Year) : Unit = {
    val parsedMeasures = sparkLocateTemperatures(year, "/stations.csv", s"/$year.csv")

    println(s"measures count : ${parsedMeasures.count()}")

    val locatedAverages = sparkLocationYearlyAverageRecords(parsedMeasures)
    val fullGrid = sparkInterpolateGrid(locatedAverages)

    visualizeRaw(fullGrid, colors).output(new java.io.File(s"$year-spark.png"))
  }

  temperatureImagePar(2002)

  def temperatureImagePar(year: Year) : Unit = {
    val startMillis = System.currentTimeMillis()
    Calendar.getInstance().getTime()
    println(s"Temperature image parallel $year - begin : ${Calendar.getInstance().getTime()}")
    val parsedMeasures = locateTemperatures(year, "/stations.csv", s"/$year.csv")
    println(s" -> Parsed measures - done in ${System.currentTimeMillis() - startMillis} [ms]")

    val locatedAverages = locationYearlyAverageRecords(parsedMeasures)
    println(s" -> Located averages - done in ${System.currentTimeMillis() - startMillis} [ms]")

    val fullGrid = parInterpolateGrid(locatedAverages)
    println(s" -> Full grid - done in ${System.currentTimeMillis() - startMillis} [ms]")

    visualizeRaw(fullGrid, colors).output(new java.io.File(s"$year.png"))
    println(s"Temperature image parallel $year - end : ${Calendar.getInstance().getTime()}")
    println(s" => Duration ${System.currentTimeMillis() - startMillis} [ms]")
  }

}
