package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{interpolateColor, predictTemperature}

import scala.math.{log, round}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = tile.location

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    scaledTile(256, 1.0)(temperatures, colors, tile)
  }

  def scaledTile(refSquare: Int, scaleFactor: Double)(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {

    def tileOrdering: Ordering[(Tile, Any)] = Ordering[(Int, Int)].on(t => (t._1.y, t._1.x))

    val tiles = tile.subTiles(round(log(refSquare)/log(2.0)).toInt)
    val pixels : Array[Pixel] =  tiles.par
      .map(tile => (tile, Pixel(interpolateColor(colors, predictTemperature(temperatures, tile.location)))))
      .toArray
      .sorted(tileOrdering)
      .map(_._2)

    Image(refSquare, refSquare, pixels).scale(scaleFactor)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    *
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data]( yearlyData: Iterable[(Year, Data)], generateImage: (Year, Tile, Data) => Unit ): Unit =
    (for (
      (year, data) <- yearlyData;
      zoom <- 0 to 3;
      x <- 0 until (1 << zoom);
      y <- 0 until (1 << zoom)
    ) yield (year, Tile(x, y , zoom), data))
    .foreach(d => generateImage(d._1, d._2, d._3))
}
