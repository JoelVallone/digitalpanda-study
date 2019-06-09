package observatory

import observatory.Extraction._
import observatory.Visualization._
import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  @transient lazy val conf: SparkConf = new SparkConf()
  .setMaster("local")
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

  //temperatureImage(1975)

  def temperatureImage(year: Year) : Unit = {
    //TODO: Troubleshoot parsing
    val parsedMeasures = sparkLocateTemperatures(year, s"/$year.csv", "/stations.csv")

    println(s"measures count : ${parsedMeasures.count()}")

    val locatedAverages = sparkAverageRecords(parsedMeasures)
    val fullGrid = sparkInterpolateGrid(locatedAverages)

    visualizeRaw(fullGrid, colors).output(new java.io.File(s"$year.png"))
  }

}
