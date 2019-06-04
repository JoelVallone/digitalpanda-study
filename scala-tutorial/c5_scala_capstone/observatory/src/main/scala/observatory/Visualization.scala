package observatory

import java.lang.Math.{abs, acos, cos, sin}

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Main.sc
import org.apache.spark.rdd.RDD

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val p = 2.0
  val earthRadiusMeters = 6371000.0

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    predictTemperatureSpark(sc.parallelize(temperatures.toSeq), location)
  }

  def predictTemperatureSpark(temperatures: RDD[(Location, Temperature)], targetLocation: Location): Temperature = {
    val weightedTemps = temperatures
      .map {case (location, temperature) => (dist(location, targetLocation), temperature)}

    val weightSum = weightedTemps
      .aggregate(0.0)((acc, wTemp) => acc + wTemp._1, _+_)

    if(weightSum != 0)
      weightedTemps
        .aggregate(0.0)((acc, wTemp) => acc + wTemp._1 * wTemp._2,_+_) / weightSum
    else 0
  }

  def dist(p: Location, q: Location): Double = {
    val centralAngle =
      if (p == q) 0
      else if (p.latRad == q.latRad || p.lonRad == q.lonRad) Math.PI
      else acos(sin(p.latRad)*sin(q.latRad) + cos(p.latRad)*cos(q.latRad) + abs(p.lonRad - q.lonRad))
    earthRadiusMeters*centralAngle
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    ???
  }

  def findLowUp(points: Iterable[(Temperature, Color)],  value: Temperature,
                low: (Temperature, Color), up: (Temperature, Color),
                min: (Temperature, Color), max: (Temperature, Color)): ((Temperature, Color),(Temperature, Color)) = {
    if (points.isEmpty)
      (if( value >= low._1 ) low else min, if( value <= up._1) up else max)
    else {
      val cand = points.head
      findLowUp(
        points.tail, value,
        lowerBound(low, cand, value),
        upperBound(low, cand, value),
        if(cand._1 < min._1) cand else min,
        if(cand._1 > max._1) cand else max)
    }

    def lowerBound(curLow:  (Temperature, Color), candLow:  (Temperature, Color), t: Temperature): (Temperature, Color) = {
      val newDelta = t - candLow._1
      val oldDelta = t - curLow._1
      if (abs(newDelta) <= abs(oldDelta) && newDelta >= 0) candLow else curLow
    }

    def upperBound(curUp:  (Temperature, Color), candUp:  (Temperature, Color), t: Temperature): (Temperature, Color) = {
      val newDelta = candUp._1 - t
      val oldDelta = curUp._1 - t
      if (abs(newDelta) <= abs(oldDelta) && newDelta >= 0) candUp else curUp
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    ???
  }

}

