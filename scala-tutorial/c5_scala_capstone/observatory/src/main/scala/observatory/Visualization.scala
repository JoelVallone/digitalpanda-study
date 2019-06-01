package observatory

import java.lang.Math.{abs, acos, cos, sin}

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val p = 2.0
  val earthRadiusMeters = 6371000.0

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    ???
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

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    ???
  }

}

