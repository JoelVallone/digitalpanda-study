package observatory

import com.sksamuel.scrimage.RGBColor
import observatory.Visualization.earthRadiusMeters

import scala.math._

/**
  * Introduced in Week 1. Represents a location on the globe.
  * @param lat Degrees of latitude, -90 ≤ lat ≤ 90
  * @param lon Degrees of longitude, -180 ≤ lon ≤ 180
  */
case class Location(lat: Double, lon: Double) {

  lazy val latRad : Double = toRadians(lat)
  lazy val lonRad : Double = toRadians(lon)
  lazy val rounded : Location = Location(round(lat), round(lon))

  // https://en.wikipedia.org/wiki/Antipodes#Mathematical_description
  def atAntipodesWith(q: Location): Boolean =
    latRad == -q.latRad && (lonRad == (q.lonRad - Pi) || lonRad == (q.lonRad + Pi))

  // https://en.wikipedia.org/wiki/Great-circle_distance
  def circleDist(q: Location): Double = {
    val centralAngle =
      if (this == q) 0
      else if (this atAntipodesWith q) Pi
      else acos(sin(latRad)*sin(q.latRad) + cos(latRad)*cos(q.latRad)*cos(abs(lonRad - q.lonRad)))
    earthRadiusMeters * centralAngle
  }
}

/**
  * Introduced in Week 3. Represents a tiled web map tile.
  * See https://en.wikipedia.org/wiki/Tiled_web_map
  * Based on http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  * @param x X coordinate of the tile
  * @param y Y coordinate of the tile
  * @param zoom Zoom level, 0 ≤ zoom ≤ 19
  */
case class Tile(x: Int, y: Int, zoom: Int) {

  // wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Mathematics
  lazy val location: Location =  Location(
        toDegrees(atan(sinh(Pi * (1.0 - 2.0 * y.toDouble / (1 << zoom))))),
        x.toDouble / (1 << zoom) * 360.0 - 180.0)

  def subTiles(zoomDepth: Int) : List[Tile]  = subTilesZoomDownTo(zoom + zoomDepth)

  private def subTilesZoomDownTo(targetZoom: Int) : List[Tile] =
    if (zoom  == targetZoom)
        this::Nil
    else
      Tile(2*x, 2*y    , zoom + 1).subTilesZoomDownTo(targetZoom) ++ Tile(2*x + 1, 2*y    , zoom + 1).subTilesZoomDownTo(targetZoom) ++
      Tile(2*x, 2*y + 1, zoom + 1).subTilesZoomDownTo(targetZoom) ++ Tile(2*x + 1, 2*y + 1, zoom + 1).subTilesZoomDownTo(targetZoom)
}

/**
  * Introduced in Week 4. Represents a point on a grid composed of
  * circles of latitudes and lines of longitude.
  * @param lat Circle of latitude in degrees, -89 ≤ lat ≤ 90
  * @param lon Line of longitude in degrees, -180 ≤ lon ≤ 179
  */
case class GridLocation(lat: Int, lon: Int) {
  lazy val location: Location = Location(lat, lon)
}

/**
  * Introduced in Week 5. Represents a point inside of a grid cell.
  * @param x X coordinate inside the cell, 0 ≤ x ≤ 1
  * @param y Y coordinate inside the cell, 0 ≤ y ≤ 1
  */
case class CellPoint(x: Double, y: Double)

/**
  * Introduced in Week 2. Represents an RGB color.
  * @param red Level of red, 0 ≤ red ≤ 255
  * @param green Level of green, 0 ≤ green ≤ 255
  * @param blue Level of blue, 0 ≤ blue ≤ 255
  */
case class Color(red: Int, green: Int, blue: Int) extends com.sksamuel.scrimage.Color {
  override def toRGB: RGBColor = RGBColor(red, green, blue, 127)
}

