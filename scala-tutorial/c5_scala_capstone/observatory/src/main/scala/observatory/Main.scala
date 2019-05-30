package observatory

import org.apache.spark.{SparkConf, SparkContext}

object Main extends App {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  @transient lazy val conf: SparkConf = new SparkConf()
  .setMaster("local")
  .setAppName("StackOverflow")
  .set("spark.driver.bindAddress", "127.0.0.1")

  @transient lazy val sc: SparkContext = new SparkContext(conf)



}
