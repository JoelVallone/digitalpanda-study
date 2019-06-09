package observatory

import java.time.LocalDate

import observatory.Main.sc
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

/**
  * 1st milestone: data extraction
  */
object Extraction {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year,
                         stationsFile: String,
                         temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    sparkLocateTemperatures(year, stationsFile, temperaturesFile).collect().seq
  }

  def sparkLocateTemperatures(year: Year,
                              stationsFile: String,
                              temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {

    val stations = loadStations(stationsFile)
    val temperatures = loadTemperatures(temperaturesFile, year)

    temperatures
      .join(stations)
      .map{ case (_,((date, temp), location)) => (date, location, temp)}
  }

  def loadStations(stationsFile : String): RDD[((String, String), Location)] =
    sc.textFile(Extraction.getClass.getResource(stationsFile).getPath)
    .map(parseStationRow)
    .filter(c => c.isDefined)
    .map(_.get)
    .partitionBy(new HashPartitioner(120))
    .persist()

  def parseStationRow(row: String): Option[((String, String), Location)] = {
    val col = row.split(",")
    if (col.length == 4) {
      val ( stn ,wban, latitude, longitude) = (col(0),col(1), col(2), col(3))
      if ( (stn.isEmpty && wban.isEmpty) || latitude.isEmpty || longitude.isEmpty)
        None
      else
        Some(((stn, wban),
          Location(latitude.toDouble, longitude.toDouble)
        ))
    }
    else
      None
  }

  def loadTemperatures(temperaturesFile : String, year: Year): RDD[((String, String), (LocalDate, Temperature))] =
      sc.textFile(Extraction.getClass.getResource(temperaturesFile).getPath)
        .map(parseTemperatureRow(year))
        .filter(c => c.isDefined)
        .map(_.get)
        .partitionBy(new HashPartitioner(120))

  def toCelsius(temperature: Temperature) : Double  = (temperature - 32.0) * 5.0/9.0

  def parseTemperatureRow(year: Int)(row: String): Option[((String, String), (LocalDate, Temperature))] = {
    val col = row.split(",")
    if (col.length == 5) {
      val (stn, wban, month, day, tempFahrenheit) = (col(0), col(1), col(2), col(3), col(4))
      if ((stn.isEmpty && wban.isEmpty) || day.isEmpty || month.isEmpty || tempFahrenheit.isEmpty)
        None
      else
        Some(((stn, wban),(
          LocalDate.of(year, month.toInt, day.toInt),
          toCelsius(tempFahrenheit.toDouble)
        )))
    }
    else
      None
  }

  /*
    def load[K,V](file: String, parser: String =>  Option[(K,V)]) : RDD[(K,V)] =
      sc.textFile(Extraction.getClass.getResource(file).getPath)
      .map(parser)
      .filter(c => c.isDefined)
      .map(_.get)
      .partitionBy(new HashPartitioner(120))
  */

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] =
    sparkAverageRecords(sc.parallelize(records.toSeq)).collect().toSeq

  def sparkAverageRecords(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
    records
      .map{ case (_, location, temp) => (location, (temp, 1))}
      .partitionBy(new HashPartitioner(120))
      .reduceByKey { case ((t1,c1), (t2,c2)) => (t1+t2, c1+c2)}
      .mapValues{case (temp, count) => if (count != 0) temp / count else 0}
  }

}
