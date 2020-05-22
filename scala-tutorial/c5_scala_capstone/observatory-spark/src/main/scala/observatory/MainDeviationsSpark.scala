package observatory

import java.io.File

import observatory.MainTemperaturesSpark.sc
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD

object MainDeviationsSpark extends ObservatorySparkApp {

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
    val refTemperatureOverYearsListRdd = for (refYear <- normalFromYear to normalToYear) yield loadYearAverageDataInSpark(refYear)

    println(s"Normals: Compute ref-averages on earth grid")
    val refTemperatureByLocation = Manipulation.computeAverageGridInSparkAndLoad(refTemperatureOverYearsListRdd)

    println(s"Normals: Broadcast to spark -> ref-averages on earth grid")
    val broadcastRefTemp = sc.broadcast(refTemperatureByLocation)

    println(s"Deviations: Load and average temperature data by location and year")
    val temperatureOverYearsListRdd = for (year <- fromYear to toYear) yield loadYearAverageDataInSpark(year)

    println(s"Deviations: Compute averages on earth grid using computed ref-averages/normals and deviation years data")
    val temperatureGridRddOverYears: Iterable[RDD[(Year, Map[GridLocation, Temperature])]] = temperatureOverYearsListRdd.par.map(Manipulation.computeDeviationsInSpark(_, broadcastRefTemp)).seq

    println(s"Deviations: Visualize deviation years data")
    generateTilesSparkProcessingGraph(
        temperatureGridRddOverYears,
        tileZoomDepth,
        refSquare = 128,
        scaleFactor = 2.0,
        colors = List.empty // TODO: define color palette
      )

    /*
    visualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile): Image
     */
  }


  private def generateTilesSparkProcessingGraph( temperatureGridRddOverYears: Iterable[RDD[(Year,  Map[GridLocation, Temperature])]],
                                                 tileZoomDepth: Int, refSquare: Int, scaleFactor: Double, colors: Iterable[(Temperature, Color)]): Unit = {
      //TODO:
      // 1) Check how to best leverage parallelism
      // 2) refactor into functions
        val broadcastColors = sc.broadcast(colors)
        (
          for (
            temperatureGridRddForYear <- temperatureGridRddOverYears;
            zoom <- 0 to tileZoomDepth;
            x <- 0 until (1 << zoom);
            y <- 0 until (1 << zoom)
          ) yield temperatureGridRddForYear.map{ case (year, temperatureByGridLoc) => (year, (Tile(x, y , zoom), temperatureByGridLoc, broadcastColors)) }
        )
          .toParArray
          .foreach( rdd =>
              rdd
                .mapValues{ case (tile, temperatureByGridLoc, broadcastColors) =>
                  val colors = broadcastColors.value
                  val grid = (gLoc: GridLocation) => temperatureByGridLoc(gLoc)
                  (tile, Visualization2.visualizeGrid(grid, colors, tile).bytes) // Scale image to avoid heavy computations
                }
                .collect()
                .foreach{ case (year, (t, imageBytes)) =>
                  val fs = FileSystem.get(sc.hadoopConfiguration)
                  val hdfsOutputDir = new Path(s"hdfs:///observatory/deviations/$year/${t.zoom}")
                  val hdfsImagePath = new Path(s"$hdfsOutputDir/${t.x}-${t.y}.png")
                  println(s"Save image tile $t of year $year in HDFS: '$hdfsImagePath'")
                  if(!fs.exists(hdfsOutputDir)) fs.mkdirs(hdfsOutputDir)
                  val output = fs.create(hdfsImagePath)
                  output.write(imageBytes)
                  output.close()
                }
          )

  }

}
