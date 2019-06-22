package observatory

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import java.io.File

import Extraction._
import Interaction._
import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Main.{readInterval, timedOp}


object MainSpark extends App {

  val workerCount : Int = 1

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.INFO)

  @transient lazy val conf: SparkConf = new SparkConf()
    .setMaster(s"local[4]")
    .setAppName("StackOverflow")
    .set("spark.driver.bindAddress", "127.0.0.1")

  @transient lazy val sc: SparkContext = new SparkContext(conf)


  val (from, to) = readInterval(args)
  timedOp(s"All tiles for years $from to $to", saveAllTiles(from, to))

  def saveAllTiles(fromYear: Int, toYear: Int): Unit = {
    val yearlyData : Iterable[(Year, RDD[(Year, Iterable[(Location, Temperature)])])] = (fromYear to toYear).toStream
      .map(year => (year, loadYearAverageData(year).groupBy(_ => year).persist()))
    generateTilesSpark(yearlyData, saveTileAsImage(128, 2.0))
  }

  private def loadYearAverageData(year: Year): RDD[(Location, Temperature)] =
   sparkLocationYearlyAverageRecords(
     sparkLocateTemperatures(year, "/stations.csv", s"/$year.csv"))

  private def saveTileAsImage(refSquare: Int, scaleFactor: Double)(yearLocatedAverages: RDD[((Year, Tile), (Year, Iterable[(Location, Temperature)]))]) : Unit =
    yearLocatedAverages
      .map{ case ((year: Year, tile: Tile), (_, data: Iterable[(Location, Temperature)])) =>
        ((year,tile), scaledTileRawPixels(refSquare)(data, colorsAbsolute, tile))
      }
    .collect()
      .foreach { case ((year: Year, t: Tile), rawPixels: Array[Int]) =>
          println(s"Save image tile $t of year $year")
          val outputDir = new File(s"target/temperatures/$year/${t.zoom}")
          if (!outputDir.exists()) outputDir.mkdirs()
          Image(refSquare, refSquare, rawPixels.map(Pixel(_)))
            .scale(scaleFactor)
            .output(new java.io.File(s"$outputDir/${t.x}-${t.y}.png"))
      }
}
