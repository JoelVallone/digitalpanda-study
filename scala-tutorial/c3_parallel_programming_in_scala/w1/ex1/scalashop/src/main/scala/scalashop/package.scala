
import common._

package object scalashop {

  /** The value of every pixel is represented as a 32 bit integer. */
  type RGBA = Int

  /** Returns the red component. */
  def red(c: RGBA): Int = (0xff000000 & c) >>> 24

  /** Returns the green component. */
  def green(c: RGBA): Int = (0x00ff0000 & c) >>> 16

  /** Returns the blue component. */
  def blue(c: RGBA): Int = (0x0000ff00 & c) >>> 8

  /** Returns the alpha component. */
  def alpha(c: RGBA): Int = (0x000000ff & c) >>> 0

  /** Used to create an RGBA value from separate components. */
  def rgba(r: Int, g: Int, b: Int, a: Int): RGBA = {
    (r << 24) | (g << 16) | (b << 8) | (a << 0)
  }

  /** Restricts the integer into the specified range. */
  def clamp(v: Int, min: Int, max: Int): Int = {
    if (v < min) min
    else if (v > max) max
    else v
  }

  /** Image is a two-dimensional matrix of pixel values. */
  class Img(val width: Int, val height: Int, private val data: Array[RGBA]) {
    def this(w: Int, h: Int) = this(w, h, new Array(w * h))
    def apply(x: Int, y: Int): RGBA = data(y * width + x)
    def update(x: Int, y: Int, c: RGBA): Unit = data(y * width + x) = c
  }

  /** Computes the blurred RGBA value of a single pixel of the input image. */
  def boxBlurKernel(src: Img, x: Int, y: Int, radius: Int): RGBA = {
    val xMax = clamp( x + radius, 0, src.width)
    val xMin = clamp( x - radius, 0, src.width)
    val yMax = clamp( y + radius, 0, src.height)
    val yMin = clamp( y - radius, 0, src.height)
    debug(s"\n(x,y)=(${x},${y}), radius=${radius}")
    debug(s"(width,height)=(${src.width},${src.height})")
    debug(s"(xMax,yMax)=(${xMax},${yMax})")
    debug(s"(xMin,yMin)=(${xMin},${yMin})")
    var (xi, yi) = (xMin, yMin)
    var (rAcc, gAcc, bAcc, aAcc) = (0, 0, 0 ,0)
    while( yi <= yMax) {
      while( xi <= xMax) {
        val pixel = src(xi, yi)
        rAcc += red(pixel); gAcc += green(pixel); bAcc += blue(pixel); aAcc += alpha(pixel)
        xi += 1
        debug(s"(xi,yi)=(${xi},${yi})")
        debug(s" - pixel=${pixel}")
        debug(s" - rAcc=${rAcc}, gAcc=${gAcc}, bAcc=${bAcc}, aAcc=${aAcc}")
      }
      xi = xMin; yi += 1
    }

    val pixelCount = (xMax - xMin + 1) * (yMax - yMin + 1)
    debug(s"pixelCount=${pixelCount}")
    if (radius != 0)
      rgba(rAcc / pixelCount , gAcc / pixelCount, bAcc / pixelCount, aAcc / pixelCount)
    else
      src(x, y)
  }



  val isDebugMode = false

  def debug(str: String): Unit
  = if (isDebugMode) println(str)


}
