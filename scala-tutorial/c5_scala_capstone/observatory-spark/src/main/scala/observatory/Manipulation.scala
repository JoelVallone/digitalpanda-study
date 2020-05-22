package observatory

import observatory.Visualization.predictTemperature
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD


/**
  * 4th milestone: value-added information (step for temperature deviation computation (step 5) and deviation display (step 6)
  */
object Manipulation {


  /**F
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val tempGrid = makeGridEager(temperatures)
    gLocation: GridLocation => tempGrid(gLocation)
  }

  def makeGridEager(temperatures: Iterable[(Location, Temperature)]): Map[GridLocation, Temperature] =
    earthGrid().par
      .map(gLocation => gLocation -> predictTemperature(temperatures, gLocation.location))
      .toMap.seq

  def loadGridFromSpark(tempForYear: RDD[(Year, Iterable[(Location, Temperature)])]): Map[GridLocation, Temperature] =
    tempForYear.map(rdd => {
      // In executor
      makeGridEager(rdd._2)
    }).first()

  /**
    * @param temperatures Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    averageEager(temperatures)
  }

  def averageEager(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val tempGrids = temperatures.map(temperatures => makeGrid(temperatures))
    val numYears = temperatures.size
    val gridAvgTemp = earthGrid().par
      .map( gLocation => gLocation -> tempGrids.map(_(gLocation)).sum / numYears)
      .toMap
    gLocation: GridLocation => gridAvgTemp(gLocation)
  }

  def computeAverageGridInSparkAndLoad(temperatureOverYears: Iterable[RDD[(Year, Iterable[(Location, Temperature)])]]): Map[GridLocation, Temperature] = {
    val tempGrids = temperatureOverYears.map(loadGridFromSpark)
    val numYears = temperatureOverYears.size
    earthGrid().par
      .map( gLocation => gLocation -> tempGrids.map(_(gLocation)).sum / numYears)
      .toMap.seq
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val tempGrid = deviationEager(temperatures, normals)
    gLocation: GridLocation => tempGrid(gLocation)
  }

  def deviationEager(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): Map[GridLocation, Temperature] = {
    val tempGrid = makeGrid(temperatures)
    earthGrid().par
      .map( gLocation => gLocation -> (tempGrid(gLocation) - normals(gLocation)))
      .toMap.seq
  }

  def computeDeviationsInSpark(temperaturesForYear: RDD[(Year, Iterable[(Location, Temperature)])],
                               normalsMapBroadcast:  Broadcast[Map[GridLocation, Temperature]]): RDD[(Year, Map[GridLocation, Temperature])] =
    temperaturesForYear
      .mapValues(temperatures => {
        // In executor
        val normalsMap = normalsMapBroadcast.value
        val normals = (gLocation: GridLocation) => normalsMap(gLocation)
        deviationEager(temperatures, normals)
      })

  private def earthGrid(): Iterable[GridLocation] =
    for {
      lat <- -89 to 90
      lon <- -180 to 179
    } yield GridLocation(lat, lon)

}

