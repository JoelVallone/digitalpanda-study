package observatory.spark

import com.sksamuel.scrimage.{Image, Pixel}
import observatory._
import org.apache.spark.rdd.RDD

// Reference: https://www.coursera.org/learn/scala-capstone/supplement/Ymsmh/value-added-information-visualization
object DeviationsSparkApp extends ObservatorySparkApp {

  timedOp("Compute", scheduleJob())

  def scheduleJob(): Unit = {
    val normalFromYear = sc.getConf.getInt("spark.observatory.deviation.normal.fromYear", 1975)
    val normalToYear = sc.getConf.getInt("spark.observatory.deviation.normal.toYear", 1989)

    val fromYear = sc.getConf.getInt("spark.observatory.deviation.tile.fromYear", 1990)
    val toYear = sc.getConf.getInt("spark.observatory.deviation.tile.toYear", 2015)
    val tileZoomDepth = sc.getConf.getInt("spark.observatory.deviation.tile.ZoomDepth", defaultValue = 3)

    println(
      s"Compute and save deviations for year $fromYear to $toYear with zoom-depth $tileZoomDepth" +
      s" using normals $normalFromYear from year to $normalToYear.")

    // Start Normals
    println(s"Normals: Load and average temperature data by location and year")
    val refTemperatureOverYearsListRdd = for (refYear <- normalFromYear to normalToYear) yield super.loadYearAverageDataInSpark(refYear)

    println(s"Normals: Compute ref-averages on earth grid")
    val refTemperatureGrid =
      Manipulation.computeAverageTempGridInSparkAndLoad(refTemperatureOverYearsListRdd)

    println(s"Normals: Broadcast to spark -> ref-averages on earth grid")
    val broadcastRefTempGrid = sc.broadcast(refTemperatureGrid)
    // Normals total exec time: 60s * NumRefYears

    println(s"Deviations: Load and average temperature data by location and year")
    val temperatureRddOverYears = for (year <- fromYear to toYear) yield (year, super.loadYearAverageDataInSpark(year))

    println(s"Deviations: Compute averages on earth grid using computed ref-averages/normals and deviation years data")
    val temperatureGridRddOverYears =
      Manipulation.computeDeviationGridsInSpark(temperatureRddOverYears, broadcastRefTempGrid)

    println(s"Deviations: Visualize deviation years data")
    Interaction.generateTilesSparkProcessingGraph(
        tileZoomDepth,
        temperatureGridRddOverYears,
        generateAndSaveImage(refSquare = 128, scaleFactor = 2.0))
    // Deviations total exec time: numYears *(45s (temperatureRdd) + 180s (temperatureGrid + visualizeGridScaledRawPixels for all tiles (very fast)))
  }

  private def generateAndSaveImage(refSquare: Int, scaleFactor: Double)
                                  (yearLocatedAverages: RDD[((Year, Tile), (Year, Map[GridLocation, Temperature]))]): Unit =
    yearLocatedAverages
        .map{ case ((year, tile), (_, tempGridMap:  Map[GridLocation, Temperature])) =>
          val grid = (gLoc: GridLocation) => tempGridMap(gLoc)
          ((year, tile), Visualization2.visualizeGridScaledRawPixels(refSquare)(grid, colorsDeviation, tile))
        }
        .collect()
        .foreach{ case ((year: Year, t: Tile), rawPixels: Array[Int]) =>
          val scaledImage = Image(refSquare, refSquare, rawPixels.map(Pixel(_))).scale(scaleFactor)
          saveTileImageToHDFS("hdfs:///observatory/deviations", year, t, scaledImage)
        }
}
