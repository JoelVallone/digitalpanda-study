package scalashop

import org.scalameter._
import common._
import Math.{max,min}

object HorizontalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val radius = 3
    val width = 1920
    val height = 1080
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqtime = standardConfig measure {
      HorizontalBoxBlur.blur(src, dst, 0, height, radius)
    }
    println(s"sequential blur time: $seqtime ms")

    val numTasks = 32
    val partime = standardConfig measure {
      HorizontalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }
}


/** A simple, trivially parallelizable computation. */
object HorizontalBoxBlur {

  /** Blurs the rows of the source image `src` into the destination image `dst`,
   *  starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each row, `blur` traverses the pixels by going from left to right.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {

    debug(s"\nHorizontalBoxBlur.blur:")
    debug(s"[from, end[ = [${from}, ${end}[")
    debug(s"(width, height)=(${src.width}, ${src.height})")

    for (
      y <- from until end;
      x <- 0 until src.width
    ) { dst(x, y) = boxBlurKernel(src, x, y, radius) }
  }

  /** Blurs the rows of the source image in parallel using `numTasks` tasks.
   *
   *  Parallelization is done by stripping the source image `src` into
   *  `numTasks` separate strips, where each strip is composed of some number of
   *  rows.
   */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {

    val workingTasks = min(numTasks, src.height)
    val increment = max(src.height / workingTasks , 1)

    info(s"\nHorizontalBoxBlur.parBlur")
    info(s"numTasks=$numTasks")
    info(s"workingTasks=$workingTasks")
    info(s"increment=$increment")
    info(s"(width, height)=(${src.width}, ${src.height})")
    info(s"(increment*workingTasks)=${increment*workingTasks}")

    val tasks = for (
      start <- 0 until increment*workingTasks by increment
    ) yield {
      val id = start / increment
      val end = if (id + 1 == workingTasks) src.height else start+increment
      info(s"id=$id, begin=$start, end=$end")
      task(blur(src, dst, start, end, radius))
    }
    tasks.foreach(_.join())

    info("End of computation")
  }

}
