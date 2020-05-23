package observatory.spark

import com.sksamuel.scrimage.{Image, Pixel}
import observatory._
import org.apache.spark.rdd.RDD

object DeviationsSparkApp extends ObservatorySparkApp {

  timedOp("Compute", scheduleJob())
  //println("press enter to stop application")
  //scala.io.StdIn.readLine()

  /*
  https://www.coursera.org/learn/scala-capstone/supplement/Ymsmh/value-added-information-visualization
  Deviation tiles generation
  Once you have implemented the above methods, you are ready to generate the tiles showing the deviations
  for all the years between 1990 and 2015, so that the final application (in the last milestone) will nicely display them:

      * Compute normals from yearly temperatures between 1975 and 1989 ;
      * Compute deviations for years between 1990 and 2015 ;
      * Generate tiles for zoom levels going from 0 to 3, showing the deviations.
         Use the output method of Image to write the tiles on your file system,
         under a location named according to the following scheme: target/deviations/<year>/<zoom>/<x>-<y>.png.

  Note that this process is going to be very CPU consuming, or might even crash if your implementation tries to load too much data into memory.
  That being said, even a smart solution performing incremental data manipulation and parallel computations might
  take a lot of time (several days). You can reduce this time by using some of these ideas:
      * Identify which parts of the process are independent and perform them in parallel ;
      * Reduce the quality of the tiles. For instance, instead of computing 256×256 images, compute 128×128 images
        (that’s going to be 4 times fewer pixels to compute) and then scale them to fit the expected tile size;
      * Reduce the quality of the spatial interpolation. For instance, instead of having grids with 360×180 points,
        you can use a grid with 120×60 points (that’s going to be 9 times fewer points to compute).
   */
  def scheduleJob(): Unit = {
    val normalFromYear = sc.getConf.getInt("spark.observatory.deviation.normal.fromYear", 1975)
    val normalToYear = sc.getConf.getInt("spark.observatory.deviation.normal.toYear", 1989)

    val fromYear = sc.getConf.getInt("spark.observatory.deviation.tile.fromYear", 1990)
    val toYear = sc.getConf.getInt("spark.observatory.deviation.tile.toYear", 2015)
    val tileZoomDepth = sc.getConf.getInt("spark.observatory.deviation.tile.ZoomDepth", defaultValue = 3)

    println(
      s"Compute and save deviations for year $fromYear to $toYear with zoom-depth $tileZoomDepth" +
      s" using normals $normalFromYear from year to $normalToYear.")

    println(s"Normals: Load and average temperature data by location and year")
    val refTemperatureOverYearsListRdd = for (refYear <- normalFromYear to normalToYear) yield super.loadYearAverageDataInSpark(refYear)

    println(s"Normals: Compute ref-averages on earth grid")
    val refTemperatureGrid = Manipulation.computeAverageTempGridInSparkAndLoad(refTemperatureOverYearsListRdd)

    println(s"Normals: Broadcast to spark -> ref-averages on earth grid")
    val broadcastRefTempGrid = sc.broadcast(refTemperatureGrid)

    println(s"Deviations: Load and average temperature data by location and year")
    val temperatureRddOverYears = for (year <- fromYear to toYear) yield (year, super.loadYearAverageDataInSpark(year))

    println(s"Deviations: Compute averages on earth grid using computed ref-averages/normals and deviation years data")
    val temperatureGridRddOverYears = Manipulation.computeDeviationGridsInSpark(temperatureRddOverYears, broadcastRefTempGrid)

    println(s"Deviations: Visualize deviation years data")
    Interaction.generateTilesSparkProcessingGraph(
        tileZoomDepth,
        temperatureGridRddOverYears,
        generateAndSaveImage(refSquare = 128, scaleFactor = 2.0))
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
