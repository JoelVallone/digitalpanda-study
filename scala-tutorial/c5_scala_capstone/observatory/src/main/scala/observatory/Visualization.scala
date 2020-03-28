package observatory


import com.sksamuel.scrimage.{Image, Pixel, RGBColor}

import scala.math.{abs, pow, round}


class Visualization {
  // https://en.wikipedia.org/wiki/Inverse_distance_weighting
  /** interpolateColor
    *
    * @param temperatures   Known temperatures: pairs containing a location and the temperature at this location
    * @param targetLocation Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature2(temperatures: Iterable[(Location, Temperature)], targetLocation: Location): Temperature = {
    //TODO: .par seems to block when more than one worker on local node compute:
    // - https://stackoverflow.com/questions/15176199/scala-parallel-collection-in-object-initializer-causes-a-program-to-hang/15176433#15176433
    val distTemps = temperatures.par
      .map { case (location, temperature) => (location circleDist targetLocation, temperature) }

    def distTemp: Ordering[(Double, Temperature)] = Ordering[Double].on(_._1)

    val minDistTemp = distTemps.min(distTemp)

    if (minDistTemp._1 < 1000.0)
      minDistTemp._2
    else {
      val weightTemps = distTemps
        .map(distTemp => (pow(1 / distTemp._1, Visualization.p), distTemp._2))
      val weightSum = weightTemps
        .aggregate(0.0)((acc, wTemp) => acc + wTemp._1, _ + _)
      if (weightSum != 0)
        weightTemps
          .aggregate(0.0)((acc, wTemp) => acc + wTemp._1 * wTemp._2, _ + _) / weightSum
      else 0
    }
  }
}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val p = 6.0

  // https://en.wikipedia.org/wiki/Inverse_distance_weighting
  /**interpolateColor
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param targetLocation Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], targetLocation: Location): Temperature = {
    //TODO: .par seems to block when more than one worker on local node compute:
    // - https://stackoverflow.com/questions/15176199/scala-parallel-collection-in-object-initializer-causes-a-program-to-hang/15176433#15176433
    val distTemps = temperatures//.par
      .map {case (location, temperature) => (location circleDist targetLocation, temperature)}

    def distTemp: Ordering[(Double, Temperature)] = Ordering[Double].on(_._1)
    val minDistTemp = distTemps.min(distTemp)

    if (minDistTemp._1 < 1000.0)
      minDistTemp._2
    else {
      val weightTemps = distTemps
        .map(distTemp => (pow(1 / distTemp._1, p), distTemp._2))
      val weightSum = weightTemps
        .aggregate(0.0)((acc, wTemp) => acc + wTemp._1, _+_)
      if(weightSum != 0)
        weightTemps
          .aggregate(0.0)((acc, wTemp) => acc + wTemp._1 * wTemp._2, _+_) / weightSum
      else 0
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {

    // https://en.wikipedia.org/wiki/Linear_interpolation
    def interpolateRGB(low: (Temperature, Int), up: (Temperature, Int), value: Temperature): Int = {
      val normDist = (value - low._1) / (up._1 - low._1)
      round(low._2 * (1 - normDist) + up._2 * normDist).intValue()
    }

    val (low, up) = findLowUp(points, value, points.head, points.head, points.head, points.head)
    if (low == up)
      low._2
    else
      Color(
        interpolateRGB((low._1, low._2.red),    (up._1, up._2.red),   value),
        interpolateRGB((low._1, low._2.green),  (up._1, up._2.green), value),
        interpolateRGB((low._1, low._2.blue),   (up._1, up._2.blue),  value)
      )
  }

  // O(N)... :-/
  def findLowUp(points: Iterable[(Temperature, Color)],
                value:  Temperature,
                lowBnd: (Temperature, Color),
                upBnd:  (Temperature, Color),
                min:    (Temperature, Color),
                max:    (Temperature, Color)): ((Temperature, Color),(Temperature, Color)) = {

    def nearestBound(delta: ((Temperature, Color),Temperature) => Temperature)
                    (cur:  (Temperature, Color), cand:  (Temperature, Color), t: Temperature): (Temperature, Color) = {
      val newDelta = delta(cand, t)
      val oldDelta = delta(cur, t)
      if (oldDelta < 0 || (abs(newDelta) <= abs(oldDelta) && newDelta >= 0)) cand else cur
    }

    if (points.isEmpty)
      (if( value >= lowBnd._1 ) lowBnd else min, if( value <= upBnd._1) upBnd else max)
    else {
      val cand = points.head
      findLowUp(
        points.tail,
        value,
        nearestBound(-_._1 + _)(lowBnd, cand, value),
        nearestBound(_._1 - _)(upBnd, cand, value),
        if(cand._1 <= min._1) cand else min,
        if(cand._1 >= max._1) cand else max
      )
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    visualizeRaw(interpolateGrid(temperatures), colors)
  }

  def visualizeRaw(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    def gpsImageOrdering: Ordering[(Location, _)] =
      Ordering[(Double, Double)].on((t: (Location, _)) => (-t._1.lat, t._1.lon))

    val pixels : Array[Pixel] =
      temperatures.par
        .map( locTemp => (locTemp._1, Pixel(toRGB(interpolateColor(colors, locTemp._2)))))
        .toArray
        .sorted(gpsImageOrdering)
        .map(_._2)
    Image(360, 180, pixels)
  }

  def interpolateGrid(temperatures: Iterable[(Location, Temperature)]) : Iterable[(Location, Temperature)] =
    (for {
      lat <- -89L to 90L
      lon <- -180L to 179L
    } yield Location(lat, lon)).toList.par
    .map(location => (location, predictTemperature(temperatures,location))).seq


  def toRGB(color : Color) : RGBColor = RGBColor(color.red, color.green, color.blue, 127)
}

