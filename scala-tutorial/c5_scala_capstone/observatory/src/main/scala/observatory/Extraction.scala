package observatory

import java.time.LocalDate

import observatory.Main.sc
import org.apache.spark.HashPartitioner
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
  def locateTemperatures(year: Year,
                         stationsFile: String,
                         temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    sparkLocateTemperatures(year, stationsFile, temperaturesFile).collect().seq
  }

  def sparkLocateTemperatures(year: Year,
                              stationsFile: String,
                              temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {
    def parseDouble(s: String) = try { Some(s.toDouble) } catch { case _ => None }
    val stations  = sc.textFile(stationsFile)
      .map( s => {
        val strings = s.split(",")
        if (strings.length == 5) {
          val ( stn:String ,wban :String,
                latitude:String, longitude:String) = strings
          if ( (stn.isEmpty && stn.isEmpty) || latitude.isEmpty || longitude.isEmpty)
            None
          else
            Some(((stn, wban), latitude.toDouble, longitude.toDouble))
        }
        else
          None
      })
      .filter(c => c.isDefined)
      .map(_.get)

    val temperatures = sc.textFile(temperaturesFile)
      .map( s => {
        val strings = s.split(",")
        if (strings.length == 6) {
          val ( stn:String, wban :String,
                day :String, month :String, tempFarenheit :String) = strings
          if ((stn.isEmpty && stn.isEmpty) || day.isEmpty || month.isEmpty || tempFarenheit.isEmpty)
            None
          else
            Some(((stn, wban), LocalDate.of(2015, 8, 11),))
        }
        else
          None
      })
      .filter(c => c.isDefined)
      .map(_.get)


/* For year 2015

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
