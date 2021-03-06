package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{interpolateColor, toRGB}

import scala.math.{log, round}

/**
  * 5th milestone: value-added information visualization
  *  - https://www.coursera.org/learn/scala-capstone/supplement/Ymsmh/value-added-information-visualization
  * Tiles showing the deviations for all the years between 1990 and 2015, so that the final application (in the last milestone)
  * will nicely display them.
  * Output: target/deviations/<year>/<zoom>/<x>-<y>.png.
  */
object Visualization2 {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val (x, y, dx, dy) = (point.x, point.y, 1.0 - point.x, 1.0 - point.y)
    d00*dx*dy + d10*x*dy + d01*dx*y + d11*x*y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val refSquare = 256
    val pixels = visualizeGridScaledRawPixels(refSquare)(grid, colors, tile).map(Pixel(_))
    Image(refSquare, refSquare, pixels)
  }

  def visualizeGridScaledRawPixels(refSquare: Int)(grid: GridLocation => Temperature,
                                                   colors: Iterable[(Temperature, Color)],
                                                   tile: Tile):  Array[Int] = {

    def tileOrdering: Ordering[(Tile, Any)] = Ordering[(Int, Int)].on(t => (t._1.y, t._1.x))

    def interpolateTileAsPixel(tile: Tile) : Pixel =
      Pixel(toRGB(interpolateColor(
        colors,
        bilinearInterpolation(
          cellPoint(tile),
          grid(gridLocationOffset(tile, 0,0)),
          grid(gridLocationOffset(tile, 0,1)),
          grid(gridLocationOffset(tile, 1,0)),
          grid(gridLocationOffset(tile, 1,1))
        )
      )))

    tile
      .subTiles(round(log(refSquare)/log(2.0)).toInt) // refSquare = 256 => 8,
      .par
      .map( tile => (tile, interpolateTileAsPixel(tile)))
      .toArray
      .sorted(tileOrdering)
      .map(_._2.toInt)
  }

  def cellPoint(tile: Tile): CellPoint =
    CellPoint(
      tile.location.lon - tile.location.floored.lon,
      tile.location.lat - tile.location.floored.lat
    )

  def gridLocationOffset(tile: Tile, lonOffset: Int, latOffset: Int): GridLocation = {
    val lat = tile.location.floored.lat.toInt + latOffset
    val lon = tile.location.floored.lon.toInt + lonOffset
    GridLocation(
      lat,  // No need to account for latitude offset as it asymptotically reaches 90/-90 ° in the tile.y coordinate
      circularLongitude(lon))
     }

  def circularLongitude(lon : Int) : Int = ((lon + 180) % 360) - 180
}
