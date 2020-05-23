package observatory

import java.time.LocalDate

import org.apache.spark.{HashPartitioner, SparkContext}
import org.apache.spark.rdd.RDD

import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val refPartitionCount : Int = 4

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.INFO)

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year,
                         stationsFile: String,
                         temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    parLocateTemperatures(year, stationsFile, temperaturesFile)
    //sparkLocateTemperatures(year, stationsFile, temperaturesFile).collect().seq
  }

  def parLocateTemperatures(year: Year,
                            stationsFile: String,
                            temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stations = parLoadStations(stationsFile)
    val temperatures = parLoadTemperatures(temperaturesFile, year)

    temperatures.par
      .flatMap( locTemps => {
        val locOpt = stations.get(locTemps._1)
        if (locOpt.isDefined) locTemps._2.map( locTemp =>  (locTemp._1, locOpt.get, locTemp._2)).toList else Nil
      }).seq
  }

  def sparkLocateTemperatures(sc: SparkContext,
                              year: Year,
                              stationsFilePath: String,
                              temperaturesFilePath: String): RDD[(LocalDate, Location, Temperature)] = {

    val stations = sparkLoadStations(sc, stationsFilePath)
    val temperatures = sparkLoadTemperatures(sc, temperaturesFilePath, year)
    temperatures
      .join(stations)
      .map{ case (_,((date, temp), location)) => (date, location, temp)}
  }


  def parLoadStations(stationsFile: String): Map[(String, String), Location] =
    Source.fromFile(Extraction.getClass.getResource(stationsFile).getPath).getLines.toArray.par
      .map(parseStationRow)
      .filter(c => c.isDefined)
      .map( opt => opt.get._1 -> opt.get._2)
      .toMap.seq

  def sparkLoadStations(sc: SparkContext, stationsFilePath : String): RDD[((String, String), Location)] =
    sc.textFile(stationsFilePath)
    .map(parseStationRow)
    .filter(c => c.isDefined)
    .map(_.get)
    .partitionBy(new HashPartitioner(refPartitionCount))
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

  def parLoadTemperatures(temperaturesFile: String, year: Year): Map[(String, String), Iterable[(LocalDate, Temperature)]] =
    Source.fromFile(Extraction.getClass.getResource(temperaturesFile).getPath).getLines.toArray
      .map(parseTemperatureRow(year))
      .filter(c => c.isDefined)
      .map( opt => opt.get._1 -> opt.get._2)
      .groupBy(_._1)
      .mapValues( v => v.map(_._2))

  def sparkLoadTemperatures(sc: SparkContext, temperaturesFilePath : String, year: Year): RDD[((String, String), (LocalDate, Temperature))] =
      sc.textFile(temperaturesFilePath)
        .map(parseTemperatureRow(year))
        .filter(c => c.isDefined)
        .map(_.get)
        .partitionBy(new HashPartitioner(1))

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

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    parLocationYearlyAverageRecords(records)
  }

  def parLocationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] =
    records.par
      .map{ case (_, location, temp) => (location, (temp, 1))}
      .groupBy(_._1)
      .mapValues( gpsTemps => {
        val (temp, count) = gpsTemps
          .map(_._2)
          .reduce((t1, t2) => (t1._1+t2._1, t1._2+t2._2))
        if (count != 0) temp / count else 0
      }).toList

  def sparkLocationYearlyAverageRecords(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] =
    records
      .map{ case (_, location, temp) => (location, (temp, 1))}
      .partitionBy(new HashPartitioner(refPartitionCount))
      .reduceByKey { case ((t1,c1), (t2,c2)) => (t1+t2, c1+c2)}
      .mapValues{case (temp, count) => if (count != 0) temp / count else 0}
}
