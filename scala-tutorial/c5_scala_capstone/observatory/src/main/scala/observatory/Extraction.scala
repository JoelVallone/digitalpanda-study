package observatory

import java.time.LocalDate

import observatory.Main.sc
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

/**
  * 1st milestone: data extraction
  */
object Extraction {

/*
For year 2015
  Stations:
    STN     ,WBAN   ,Latitude ,	Longitude
    010013  ,       ,         ,           => (0) rejected as it has no coordinates
    724017  ,03707  ,+37.358  ,-078.438   => (1) ok
    724017  ,       ,+37.350  ,-078.433   => (2) ok

  Temperatures:
    STN     ,WBAN   ,Month    ,Day  ,Temperature (Fahrenheit)
    010013  ,       ,11       ,25   ,39.2  => rejected as station (0) has no coordinates
    724017  ,       ,08       ,11   ,81.14 => ok, with station  (2)
    724017  ,03707  ,12       ,06   ,32    => ok, with station  (1)
    724017  ,03707  ,01       ,29   ,35.6  => ok, with station  (1)

  Result:
    Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0)
    )
*/
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

    def toCelsius(temperature: Temperature) : Double
    = (temperature -32) * 5/9
    def locationKey(stn:String, wban :String) : String
    = stn + "-" + wban

    val stations :  RDD[((String, String), Location)] =
      sc.textFile(stationsFile)
        .map( s => {
          val strings = s.split(",")
          if (strings.length == 5) {
            val ( stn:String ,wban :String,
                  latitude:String, longitude:String) = strings
            if ( (stn.isEmpty && stn.isEmpty) || latitude.isEmpty || longitude.isEmpty)
              None
            else
              Some(
                (
                  (stn, wban),
                  Location(latitude.toDouble, longitude.toDouble)
                )
              )
          }
          else
            None
        })
        .filter(c => c.isDefined)
        .map(_.get)
        .partitionBy(new HashPartitioner(42))
        .persist()

    val temperatures: RDD[((String, String), (LocalDate, Temperature))] =
      sc.textFile(temperaturesFile)
        .map( s => {
          val strings = s.split(",")
          if (strings.length == 6) {
            val ( stn:String, wban :String,
                  day :String, month :String, tempFahrenheit :String) = strings
            if ((stn.isEmpty && stn.isEmpty) || day.isEmpty || month.isEmpty || tempFahrenheit.isEmpty)
              None
            else
              Some((
                  (stn, wban),
                  (
                    LocalDate.of(year.toInt, month.toInt, day.toInt),
                    toCelsius(tempFahrenheit.toDouble)
                  ))
              )
          }
          else
            None
        })
        .filter(c => c.isDefined)
        .map(_.get)
        .partitionBy(new HashPartitioner(42))
        .persist()

    temperatures
      .join(stations)
      .map{ case (_,((date, temp), location)) => (date, location, temp)}
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] =
    sparkAverageRecords(sc.parallelize(records.toSeq)).collect().toSeq

  def sparkAverageRecords(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
    records
      .map{ case (_, location, temp) => (location, (temp, 1))}
      .partitionBy(new HashPartitioner(42))
      .persist()
      .reduceByKey { case ((t1,c1), (t2,c2)) => (t1+t2, c1+c2)}
      .mapValues{case (temp, count) => if (count != 0) temp / count else 0}
  }

}
