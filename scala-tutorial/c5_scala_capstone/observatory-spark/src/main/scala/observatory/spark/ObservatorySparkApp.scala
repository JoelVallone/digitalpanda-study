package observatory.spark

import java.util.Calendar

import com.sksamuel.scrimage.Image
import observatory.Extraction.{sparkLocateTemperatures, sparkLocationYearlyAverageRecords}
import observatory.{Location, Temperature, Tile, Year}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

trait ObservatorySparkApp extends App{

  val dataFolder = "hdfs:///scala-capstone-data/"
  val refPartitionCount : Int = 4

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.DEBUG)

  @transient lazy val conf: SparkConf = new SparkConf()
    .setAppName("Observatory")

  @transient lazy val sc: SparkContext = new SparkContext(conf)

  def timedOp(opName: String , computation: => Unit) : Unit = {
    val startMillis = System.currentTimeMillis()
    println(s"$opName start : ${Calendar.getInstance().getTime}")
    computation
    println(s"$opName - end : ${Calendar.getInstance().getTime}")
    println(s" => Total duration: ${System.currentTimeMillis() - startMillis} [ms]")
  }

  def loadYearAverageDataInSpark(year: Year): RDD[(Year, Iterable[(Location, Temperature)])] = {
    sparkLocationYearlyAverageRecords(
      sparkLocateTemperatures(year, s"$dataFolder/stations.csv", s"$dataFolder/$year.csv")
    )
      .groupBy(_ => year)  // only one year loaded in the rdd as the file loaded is specific for a year => RDD with only 1 row !
      .partitionBy(new HashPartitioner(1))
      .persist()
  }

  def saveTileImageToHDFS (hdfsDirpath: String, year: Year, t: Tile, image: Image): Unit = {
    val fs = FileSystem.get(sc.hadoopConfiguration)
    val hdfsOutputDir = new Path(s"$hdfsDirpath/$year/${t.zoom}")
    val hdfsImagePath = new Path(s"$hdfsOutputDir/${t.x}-${t.y}.png")
    println(s"Save image tile $t of year $year in HDFS: '$hdfsImagePath'")
    if(!fs.exists(hdfsOutputDir)) fs.mkdirs(hdfsOutputDir)
    val output = fs.create(hdfsImagePath)
    output.write(image.bytes)
    output.close()
  }
}
