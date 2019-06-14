package observatory

import java.io.File
import java.util.Calendar

import observatory.Interaction.tile
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

  //timedOp("Full image for a year", visualizeYear(2002))
  //timedOp("Tile for a year", saveTileForYear(2002, Tile(0, 0, 0)))
  //timedOp("Tile for a year", saveTileForYear(2002, Tile(1, 1, 1)))
  timedOp("All tiles for all years", saveAllTiles())

  def visualizeYear(year: Year) : Unit = {
    val yearData = loadYearAverageData(year)
    val startMillis = System.currentTimeMillis()
    Visualization.visualize(yearData, colors).output(new java.io.File(s"$year.png"))
    println(s" -> Visualization.visualize done in ${System.currentTimeMillis() - startMillis} [ms]")

  }

  def saveTileForYear(year: Year, targetTile: Tile) : Unit = {
    val yearData = loadYearAverageData(year)
    saveTileAsImage(year, targetTile, yearData)
  }

  def saveAllTiles(): Unit = {
    val yearlyData = (1975 to 2015).toStream.map(year => (year, loadYearAverageData(year)))
    Interaction.generateTiles(yearlyData, saveTileAsImage)
  }

  private def saveTileAsImage(year: Year, t: Tile, locatedAverages: Iterable[(Location, Temperature)]): Unit = {
    val tileStartMillis = System.currentTimeMillis()
    val image = tile(locatedAverages, colors, t)
    println(s" -> $t as image done in: ${System.currentTimeMillis() - tileStartMillis} [ms]")

    val outputDir = new File(s"target/temperatures/$year/${t.zoom}")
    if (!outputDir.exists()) outputDir.mkdirs()
    image.output(new java.io.File(s"$outputDir/${t.x}-${t.y}.png"))
  }

  private def loadYearAverageData(year: Year): Iterable[(Location, Temperature)] = {
    import Extraction._

    var startMillis = System.currentTimeMillis()
    val parsedMeasures = locateTemperatures(year, "/stations.csv", s"/$year.csv")
    println(s" -> Parsed measures of year $year in: ${System.currentTimeMillis() - startMillis} [ms]")

    startMillis = System.currentTimeMillis()
    val locatedAverages = locationYearlyAverageRecords(parsedMeasures)
    println(s" -> Located averages done in: ${System.currentTimeMillis() - startMillis} [ms]")

    locatedAverages
  }

  private def timedOp(opName: String , computation: => Unit) : Unit = {
    val startMillis = System.currentTimeMillis()
    println(s"$opName start : ${Calendar.getInstance().getTime()}")
    computation
    println(s"$opName - end : ${Calendar.getInstance().getTime()}")
    println(s" => Total duration: ${System.currentTimeMillis() - startMillis} [ms]")
  }

}
