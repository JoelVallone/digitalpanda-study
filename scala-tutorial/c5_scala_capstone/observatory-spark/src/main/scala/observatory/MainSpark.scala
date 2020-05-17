package observatory

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}
import java.io.File

import Extraction._
import Interaction._
import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Main.timedOp
import org.apache.hadoop.fs.{FileSystem, Path}


object MainSpark extends App {

  val dataFolder = "hdfs:///scala-capstone-data/"
  val workerCount : Int = 2

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.DEBUG)

  @transient lazy val conf: SparkConf = new SparkConf()
    .setAppName("Observatory")
    //https://stackoverflow.com/questions/42234447/running-from-a-local-ide-against-a-remote-spark-cluster
    //.setMaster(s"fanless1.digitalpanda.org[$workerCount]")
    //.set("spark.executor.memory", "1g")
    //.set("spark.executor.cores", "1")
    //.set("spark.driver.bindAddress", "fanless1.digitalpanda.org") // 192.168.1.1 or fanless1.digitalpanda.org
   //val hdfs_master_uri : String = "hdfs://fanless1.digitalpanda.org"
  @transient lazy val sc: SparkContext = new SparkContext(conf)

  timedOp("Compute tiles", computeTiles())
  //println("press enter to stop application")
  //scala.io.StdIn.readLine()

  def computeTiles(): Unit = {

    val fromYear = sc.getConf.getInt("spark.observatory.tile.fromYear", 1975)
    val toYear = sc.getConf.getInt("spark.observatory.tile.toYear", 1975)
    val tileZoomLevel = sc.getConf.getInt("spark.observatory.tile.ZoomDepth", defaultValue = 3)

    //TODO: display number of  computed tiles in message, optimize memory footprint when many years
    println(s"Compute and save tiles for years $fromYear to $toYear with zoom-level $tileZoomLevel.")

    println(s"Load and average temperature data by location and year")
    val yearlyData : Iterable[(Year, RDD[(Year, Iterable[(Location, Temperature)])])] =
      (fromYear to toYear)
        .toStream
        .map(year => (year, loadYearAverageData(year)))

    println(s"Generate & save tiles")
    generateTilesSparkProcessingGraph(
      tileZoomLevel,
      yearlyData,
      saveTileAsImage(
        refSquare = 128,
        scaleFactor = 2.0,
        sc.getConf.getBoolean("spark.observatory.tile.doSaveToLocalFS", defaultValue = true),
        sc.getConf.getBoolean("spark.observatory.tile.doSaveTilesToHDFS", defaultValue = false)))
    }

  private def loadYearAverageData(year: Year): RDD[(Year, Iterable[(Location, Temperature)])] = {
    sparkLocationYearlyAverageRecords(
      sparkLocateTemperatures(year, s"$dataFolder/stations.csv", s"$dataFolder/$year.csv")
    )
      .groupBy(_ => year)
      .partitionBy(new HashPartitioner(workerCount))
      .persist()
  }

  private def saveTileAsImage(refSquare: Int, scaleFactor: Double, doSaveToHDFS: Boolean, doSaveToLocalFS: Boolean)
                             (yearLocatedAverages: RDD[((Year, Tile), (Year, Iterable[(Location, Temperature)]))]): Unit = {

    val saveTileImageToHDFS = (year: Year, t: Tile, image: Image) => {
      val fs = FileSystem.get(sc.hadoopConfiguration)
      val hdfsOutputDir = new Path(s"hdfs:///observatory/temperatures/$year/${t.zoom}")
      val hdfsImagePath = new Path(s"$hdfsOutputDir/${t.x}-${t.y}.png")
      println(s"Save image tile $t of year $year in HDFS: '$hdfsImagePath'")
      if(!fs.exists(hdfsOutputDir)) fs.mkdirs(hdfsOutputDir)
      val output = fs.create(hdfsImagePath)
      output.write(image.bytes)
      output.close()
    }: Unit

    val saveTileImageToLocalFS = (year: Year, t: Tile, image: Image) => {
      val outputDir = new File(s"target/spark/temperatures/$year/${t.zoom}")
      val filePath = s"$outputDir/${t.x}-${t.y}.png"
      println(s"Save image tile $t of year $year in local filesystem: '$filePath'")
      if(!outputDir.exists()) outputDir.mkdirs()
      image.output(new java.io.File(s"$outputDir/${t.x}-${t.y}.png"))
    }: Unit

    yearLocatedAverages
      .map { case ((year: Year, tile: Tile), (_, data: Iterable[(Location, Temperature)])) =>
        ((year, tile), scaledTileRawPixels(refSquare)(data, colorsAbsolute, tile))
      }
      .collect()
      .foreach { case ((year: Year, t: Tile), rawPixels: Array[Int]) =>
        val scaledImage = Image(refSquare, refSquare, rawPixels.map(Pixel(_))).scale(scaleFactor)
        if (doSaveToHDFS) saveTileImageToHDFS(year, t, scaledImage)
        if (doSaveToLocalFS) saveTileImageToLocalFS(year, t, scaledImage)
      }
  }
}
