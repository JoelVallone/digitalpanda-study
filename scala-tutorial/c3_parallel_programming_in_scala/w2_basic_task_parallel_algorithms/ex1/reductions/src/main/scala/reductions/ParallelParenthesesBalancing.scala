package reductions

import scala.annotation._
import org.scalameter._
import common._

import scala.math.max

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def balance(charsIn: Array[Char]): Boolean = {
    def balancePartial(chars: Array[Char], pos:Int, opening: Int): Boolean = {
      if (opening < 0 || pos >= chars.length ) opening == 0
      else {
        if (chars(pos) == '(')
          balancePartial(chars, pos + 1, opening + 1)
        else if (chars(pos) == ')')
          balancePartial(chars, pos + 1, opening - 1)
        else
          balancePartial(chars, pos + 1, opening)
      }

    }
    charsIn == null || charsIn.length == 0  || balancePartial(charsIn, 0, 0)
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(idx: Int, until: Int, opening: Int, closing: Int): (Int, Int) = {
        if (idx >= until) (opening, closing)
        else if (idx == until - 1)
          (if (chars(idx) == '(') opening + 1 else opening, if (chars(idx) == ')') closing + 1 else closing)
        else {
          val leftChar = chars(idx)
          val rightChar = chars(until-1)
          if (leftChar == '(')
            if (rightChar == ')')
              traverse(idx + 1, until - 1, opening, closing)
            else if(rightChar == '(')
              traverse(idx , until - 1, opening + 1, closing)
            else
              traverse(idx , until - 1, opening, closing)
          else if (leftChar == ')')
              traverse(idx + 1 , until, opening, closing + 1)
          else
            traverse(idx + 1 , until, opening, closing)
        }
    }

    def reduce(from: Int, until: Int) : (Int, Int) = {
      if (from >= until || ((until - from) <= threshold)) {
        val res = traverse(from, until,0,0)
        //println(s"  -> [$from,$until[, ${chars.slice(from,until).mkString("")} => (=${res._1}, )=${res._2}")
        res
      }
      else {
        val middle = from + ((until - from) >> 1 )
        val LR = parallel(reduce(from, middle), reduce(middle, until))
        val leftOpening = LR._1._1
        val leftClosing = LR._1._2
        val rightOpening = LR._2._1
        val rightClosing = LR._2._2
        val opening = rightOpening + max(leftOpening - rightClosing, 0)
        val closing = leftClosing + max(rightClosing - leftOpening, 0)
        //println(s" - [$from,$until[, ${chars.slice(from,until).mkString("")} => L(=${LR._1._1}, L)=${LR._1._2}, R(=${LR._2._1}, R)=${LR._2._2}, (=$opening, )=$closing")
        (opening, closing)
      }
    }
    {
      //println(s"\n\nReduce: [0,${chars.length}[, treshold=$threshold, '${chars.mkString("")}'")
      reduce(0, chars.length) == (0, 0)
    }
  }

  // For those who want more:
  // Prove that your reduction operator is associative!

}
