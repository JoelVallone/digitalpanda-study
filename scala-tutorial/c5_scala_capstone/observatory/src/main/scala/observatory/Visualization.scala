package observatory

import java.lang.Math._

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Main.sc
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val p = 2.0
  val earthRadiusMeters = 6371000.0

  /**interpolateColor
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    predictTemperatureSpark(sc.parallelize(temperatures.toSeq), location)
  }

  // https://en.wikipedia.org/wiki/Inverse_distance_weighting
  def predictTemperatureSpark(temperatures: RDD[(Location, Temperature)], targetLocation: Location): Temperature = {
    val distTemps = temperatures
      .map {case (location, temperature) => (circleDist(location, targetLocation), temperature)}
      .persist()

    def distTemp: Ordering[(Double, Temperature)] = Ordering[Double].on(_._1)
    val minDistTemp = distTemps.min()(distTemp)

    if (minDistTemp._1 < 1000)
      minDistTemp._2
    else {
      val weightTemps = distTemps
        .map(distTemp => (pow(1 / distTemp._1, p), distTemp._2))
        .persist()
      val weightSum = weightTemps
        .aggregate(0.0)((acc, wTemp) => acc + wTemp._1, _+_)
      if(weightSum != 0)
        weightTemps
          .aggregate(0.0)((acc, wTemp) => acc + wTemp._1 * wTemp._2,_+_) / weightSum
      else 0
    }
  }

  // https://en.wikipedia.org/wiki/Great-circle_distance
  def circleDist(p: Location, q: Location): Double = {

    def areAntipodes(p: Location, q: Location) : Boolean =
      p.latRad == -q.latRad &&
        (p.lonRad == (q.lonRad - PI) || p.lonRad == (q.lonRad + PI))

    val centralAngle =
      if (p == q) 0
      else if (areAntipodes(p,q)) PI
      else acos(sin(p.latRad)*sin(q.latRad) + cos(p.latRad)*cos(q.latRad)*cos(abs(p.lonRad - q.lonRad)))
    earthRadiusMeters*centralAngle
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
  def findLowUp(points: Iterable[(Temperature, Color)], value: Temperature,
                lowBnd: (Temperature, Color), upBnd: (Temperature, Color),
                min: (Temperature, Color), max: (Temperature, Color)): ((Temperature, Color),(Temperature, Color)) = {

    def nearestBound(delta: ((Temperature, Color),Temperature) => Temperature)
                    (cur:  (Temperature, Color), cand:  (Temperature, Color), t: Temperature): (Temperature, Color) = {
      val newDelta = delta(cand, t)
      val oldDelta = delta(cur, t)
      if (abs(newDelta) <= abs(oldDelta) && newDelta >= 0) cand else cur
    }

    if (points.isEmpty)
      (if( value >= lowBnd._1 ) lowBnd else min, if( value <= upBnd._1) upBnd else max)
    else {
      val cand = points.head
      findLowUp(
        points.tail, value,
        nearestBound(-_._1 + _)(lowBnd, cand, value),   nearestBound(_._1 - _)(upBnd, cand, value),
        if(cand._1 < min._1) cand else min,             if(cand._1 > max._1) cand else max
      )
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    def gpsImageOrdering: Ordering[(Location, _)] =
      Ordering[(Double, Double)].on((t: (Location, _)) => (-t._1.lat, t._1.lon))

     val pixels : Array[Pixel] =
       interpolateGrid(temperatures).par
        .map( locTemp => (locTemp._1, Pixel(interpolateColor(colors, locTemp._2))))
      .toArray
        .sorted(gpsImageOrdering)
        .map(_._2)
    Image(360, 180, pixels)
  }

  def interpolateGrid(temperatures: Iterable[(Location, Temperature)]) : Iterable[(Location, Temperature)] = {
    val temperaturesRdd = sc.parallelize(temperatures.toSeq).persist()
    for {
      lat <- -180L to 179L
      lon <- -89 to 90
    } yield {
      val location = Location(lat, lon)
      (location, predictTemperatureSpark(temperaturesRdd, location))
    }
  }
}

