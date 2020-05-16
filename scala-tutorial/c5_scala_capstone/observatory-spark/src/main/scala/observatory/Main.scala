package observatory

import java.io.File
import java.util.Calendar

import observatory.Interaction.scaledTile

object Main extends App {

  /*
    To Start the computations :
    > SBT_OPTS="-Xms512M -Xmx14G -Xss2M -XX:MaxMetaspaceSize=1024M" sbt "run 1975 2015" & cpulimit -l 700 -p $! -b
   */

  //timedOp("Full image for a year", visualizeYear(2002))
  timedOp("Tile for a year", saveTileForYear(2002, Tile(0, 0, 0)))
  //timedOp("Tile for a year", saveTileForYear(2002, Tile(1, 1, 1)))
  //val (from, to) = readInterval(args)
  //timedOp("All tiles for all years", saveAllTiles(from, to))

  def visualizeYear(year: Year) : Unit = {
    val yearData = loadYearAverageData(year)
    val startMillis = System.currentTimeMillis()
    Visualization.visualize(yearData, colorsAbsolute).output(new java.io.File(s"$year.png"))
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
    val image = scaledTile(128,2.0)(locatedAverages, colorsAbsolute, t)
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

  def timedOp(opName: String , computation: => Unit) : Unit = {
    val startMillis = System.currentTimeMillis()
    println(s"$opName start : ${Calendar.getInstance().getTime()}")
    computation
    println(s"$opName - end : ${Calendar.getInstance().getTime()}")
    println(s" => Total duration: ${System.currentTimeMillis() - startMillis} [ms]")
  }

  def readInterval(arguments: Array[String]):(Int, Int) = {

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

    if (arguments.length < 2) {
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
