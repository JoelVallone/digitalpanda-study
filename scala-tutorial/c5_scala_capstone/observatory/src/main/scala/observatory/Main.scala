package observatory

import java.io.File
import java.util.Calendar

import observatory.Interaction.scaledTile
import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  /*
    To Start the computations :
    > SBT_OPTS="-Xms512M -Xmx14G -Xss2M -XX:MaxMetaspaceSize=1024M" sbt "run 1975 2015" & cpulimit -l 700 -p $! -b
   */

  val workerCount : Int = 1

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  @transient lazy val conf: SparkConf = new SparkConf()
  .setMaster(s"local")
  .setAppName("StackOverflow")
  .set("spark.driver.bindAddress", "127.0.0.1")

  @transient lazy val sc: SparkContext = new SparkContext(conf)

  val colors: Iterable[(Temperature, Color)] = Seq(
    (60,  Color(255,  255,  255)),
    (32,  Color(255,  0,    0)),
    (12,  Color(255,  255,  0)),
    (0,   Color(0,    255,  255)),
    (-15,	Color(0,    0,    255)),
    (-27,	Color(255,  0,    255)),
    (-50,	Color(33,   0,    107)),
    (-60,	Color(0,    0,    0))
  )

  //timedOp("Full image for a year", visualizeYear(2002))
  //timedOp("Tile for a year", saveTileForYear(2002, Tile(0, 0, 0)))
  //timedOp("Tile for a year", saveTileForYear(2002, Tile(1, 1, 1)))
  val (from, to) = readInterval()
  timedOp("All tiles for all years", saveAllTiles(from, to))

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

  def saveAllTiles(fromYear: Int, toYear: Int): Unit = {
    val yearlyData = (fromYear to toYear).toStream.map(year => (year, loadYearAverageData(year)))
    Interaction.generateTiles(yearlyData, saveTileAsImage)
  }

  private def saveTileAsImage(year: Year, t: Tile, locatedAverages: Iterable[(Location, Temperature)]): Unit = {
    val tileStartMillis = System.currentTimeMillis()
    val image = scaledTile(128,2.0)(locatedAverages, colors, t)
    println(s" -> $t of year $year as image done in: ${System.currentTimeMillis() - tileStartMillis} [ms]")

    val outputDir = new File(s"target/temperatures/$year/${t.zoom}")
    if (!outputDir.exists()) outputDir.mkdirs()
    image.output(new java.io.File(s"$outputDir/${t.x}-${t.y}.png"))
  }

  private def loadYearAverageData(year: Year): Iterable[(Location, Temperature)] = {
    import Extraction._

    val parseStartMillis = System.currentTimeMillis()
    val parsedMeasures = locateTemperatures(year, "/stations.csv", s"/$year.csv")
    println(s" -> Parsed measures of year $year in: ${System.currentTimeMillis() - parseStartMillis} [ms]")

    val averageStartMillis = System.currentTimeMillis()
    val locatedAverages = locationYearlyAverageRecords(parsedMeasures)
    println(s" -> Located averages of year $year done in: ${System.currentTimeMillis() - averageStartMillis} [ms]")

    locatedAverages
  }

  private def timedOp(opName: String , computation: => Unit) : Unit = {
    val startMillis = System.currentTimeMillis()
    println(s"$opName start : ${Calendar.getInstance().getTime()}")
    computation
    println(s"$opName - end : ${Calendar.getInstance().getTime()}")
    println(s" => Total duration: ${System.currentTimeMillis() - startMillis} [ms]")
  }

  private def readInterval():(Int, Int) = {

    val defaultFrom = 1975
    val defaultTo = 2015

    def readIntOrDefault(idx: Int, default: Int): Int =
      try {
        Integer.valueOf(args(idx))
      } catch {
        case _: Exception => default
      }

    def defaultInterval(): (Int, Int) = {
      System.err.println(
        s"Warning: Need two input argument specifying an interval between 1975 and 2015. \n" +
        s"Using default interval of $defaultFrom to $defaultTo")
      (defaultFrom, defaultTo)
    }

    if (args.length < 2) {
      defaultInterval()
    } else {
      val from = readIntOrDefault(0, defaultFrom)
      val to = readIntOrDefault(1, defaultTo)
      if (from >= 1975 && to <= 2015 && from <= to) {
        println(s"Using interval from $from to $to")
        (from, to)
      } else {
        defaultInterval()
      }
    }
  }


}
