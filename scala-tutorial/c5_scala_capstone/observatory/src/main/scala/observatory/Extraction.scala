package observatory

import java.time.LocalDate

import observatory.Main.sc
import org.apache.spark.rdd.RDD

/**
  * 1st milestone: data extraction
  */
object Extraction {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    sparkLocateTemperatures(year, stationsFile, temperaturesFile).collect().seq
  }

  def sparkLocateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {
    val tempLines: RDD[String]  = sc.textFile(temperaturesFile)
    val stationLines: RDD[String]  = sc.textFile(stationsFile)
    ???
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    sparkAverageRecords(sc.parallelize(records)).collect().toSeq
  }

  def sparkAverageRecords(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
     records
        .map(t => (t._2, t._3))
        .groupBy(_._1)
        .mapValues(
          measures => {
            val (sum, count) = measures.par
              .map(t => (t._2, 1))
              .fold(0.0, 0)((t1, t2) => (t1._1 + t2._1, t1._2 + t2._2))
              if (count != 0) sum / count else 0
          }
        )

  }

}
