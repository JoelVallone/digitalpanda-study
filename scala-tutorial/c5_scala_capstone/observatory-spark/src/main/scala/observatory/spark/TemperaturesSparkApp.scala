package observatory.spark

import java.io.File

import com.sksamuel.scrimage.{Image, Pixel}
import observatory._
import org.apache.spark.rdd.RDD


// Reference: https://www.coursera.org/learn/scala-capstone/supplement/0i1IJ/milestone-interactive-visualization-in-a-web-app
object TemperaturesSparkApp extends ObservatorySparkApp {

  timedOp("Compute tiles", computeTiles())

  def computeTiles(): Unit = {

    val fromYear = sc.getConf.getInt("spark.observatory.tile.fromYear", 1975)
    val toYear = sc.getConf.getInt("spark.observatory.tile.toYear", 1975)
    val tileZoomLevel = sc.getConf.getInt("spark.observatory.tile.ZoomDepth", defaultValue = 3)

    println(s"Compute and save tiles for years $fromYear to $toYear with zoom-level $tileZoomLevel.")

    println(s"Load and average temperature data by location and year")
    val yearlyData : Iterable[(Year, RDD[(Year, Iterable[(Location, Temperature)])])] =
      (fromYear to toYear)
        .toStream
        .map(year => (year, loadYearAverageDataInSpark(year)))

    println(s"Generate & save tiles")
    Interaction.generateTilesSparkProcessingGraph(
      tileZoomLevel,
      yearlyData,
      generateAndSaveImage(
        refSquare = 128,
        scaleFactor = 2.0,
        sc.getConf.getBoolean("spark.observatory.tile.doSaveToLocalFS", defaultValue = true),
        sc.getConf.getBoolean("spark.observatory.tile.doSaveTilesToHDFS", defaultValue = false)))
    }

  private def generateAndSaveImage(refSquare: Int, scaleFactor: Double, doSaveToHDFS: Boolean, doSaveToLocalFS: Boolean)
                                  (yearLocatedAverages: RDD[((Year, Tile), (Year, Iterable[(Location, Temperature)]))]): Unit = {

    val saveTileImageToLocalFS = (year: Year, t: Tile, image: Image) => {
      val outputDir = new File(s"target/spark/temperatures/$year/${t.zoom}")
      val filePath = s"$outputDir/${t.x}-${t.y}.png"
      println(s"Save image tile $t of year $year in local filesystem: '$filePath'")
      if(!outputDir.exists()) outputDir.mkdirs()
      image.output(new java.io.File(s"$outputDir/${t.x}-${t.y}.png"))
    }: Unit

    yearLocatedAverages
      .map { case ((year: Year, tile: Tile), (_, data: Iterable[(Location, Temperature)])) =>
        ((year, tile), Interaction.scaledTileRawPixels(refSquare)(data, colorsAbsolute, tile))
      }
      .collect()
      .foreach { case ((year: Year, t: Tile), rawPixels: Array[Int]) =>
        val scaledImage = Image(refSquare, refSquare, rawPixels.map(Pixel(_))).scale(scaleFactor)
        if (doSaveToHDFS) saveTileImageToHDFS("hdfs:///observatory/temperatures", year, t, scaledImage)
        if (doSaveToLocalFS) saveTileImageToLocalFS(year, t, scaledImage)
      }
  }
}
