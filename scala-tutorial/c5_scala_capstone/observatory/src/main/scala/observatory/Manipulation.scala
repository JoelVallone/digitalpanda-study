package observatory

import observatory.Visualization.predictTemperature


/**
  * 4th milestone: value-added information
  */
object Manipulation {


  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    makeGridEager(temperatures)
  }

  def makeGridEager(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val gridTemp = earthGrid().par
      .map(gLocation => gLocation -> predictTemperature(temperatures, gLocation.location))
      .toMap
    gLocation: GridLocation => gridTemp(gLocation)
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    averageEager(temperaturess)
  }

  def averageEager(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val tempGrids = temperaturess.map(temperatures => makeGrid(temperatures))
    val numYears = temperaturess.size
    val gridAvgTemp = earthGrid().par
      .map( gLocation => gLocation -> tempGrids.map(_(gLocation)).sum / numYears)
      .toMap
    gLocation: GridLocation => gridAvgTemp(gLocation)
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    deviationEager(temperatures, normals)
  }

  def deviationEager(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val tempGrid = makeGrid(temperatures)
    val gridDevTemp = earthGrid()
      .map( gLocation => gLocation -> (tempGrid(gLocation) - normals(gLocation)))
      .toMap
    gLocation: GridLocation => gridDevTemp(gLocation)
  }

  private def earthGrid(): Iterable[GridLocation] =
    for {
      lat <- -89 to 90
      lon <- -180 to 179
    } yield GridLocation(lat, lon)

}

