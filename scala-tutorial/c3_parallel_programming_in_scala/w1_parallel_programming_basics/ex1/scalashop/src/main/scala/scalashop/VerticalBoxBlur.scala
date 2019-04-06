package scalashop

import java.lang.Math.min

import org.scalameter._
import common._

object VerticalBoxBlurRunner {

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
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqtime ms")

    val numTasks = 32
    val partime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }

}

/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur {

  /** Blurs the columns of the source image `src` into the destination image
   *  `dst`, starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each column, `blur` traverses the pixels by going from top to
   *  bottom.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {

    debug(s"\nHorizontalBoxBlur.blur:")
    debug(s"[from, end[ = [${from}, ${end}[")
    debug(s"(width, height)=(${src.width}, ${src.height})")

    for (
      y <- 0 until src.height;
      x <- from until end
    ) { dst(x, y) = boxBlurKernel(src, x, y, radius) }
  }

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
   *
   *  Parallelization is done by stripping the source image `src` into
   *  `numTasks` separate strips, where each strip is composed of some number of
   *  columns.
   */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {

    val workingTasks = min(numTasks, src.width)
    val increment = math.max(src.width / workingTasks, 1)

    info(s"\nVerticalBoxBlur.parBlur")
    info(s"(width, height)=(${src.width}, ${src.height})")
    info(s"numTasks=${numTasks}")
    info(s"workingTasks=${workingTasks}")
    info(s"increment=${increment}")
    info(s"(increment*numTasks)=${increment*workingTasks}")

    val tasks = for (
      start <- 0 until increment*workingTasks by increment
    ) yield {
      val id = start / increment
      val end = if (id + 1 == workingTasks) src.width else start+increment
      println(s"id=$id, begin=$start, end=$end")
      task(blur(src, dst, start, end, radius))
    }
    tasks.foreach(_.join())

    info("End of computation")
  }

}
