package reductions

import org.scalameter._
import common._

object ParallelCountChangeRunner {

  @volatile var seqResult = 0

  @volatile var parResult = 0

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 20,
    Key.exec.maxWarmupRuns -> 40,
    Key.exec.benchRuns -> 80,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val amount = 250
    val coins = List(1, 2, 5, 10, 20, 50)
    val seqtime = standardConfig measure {
      seqResult = ParallelCountChange.countChange(amount, coins)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential count time: $seqtime ms")

    def measureParallelCountChange(threshold: ParallelCountChange.Threshold): Unit = {
      val fjtime = standardConfig measure {
        parResult = ParallelCountChange.parCountChange(amount, coins, threshold)
      }
      println(s"parallel result = $parResult")
      println(s"parallel count time: $fjtime ms")
      println(s"speedup: ${seqtime / fjtime}")
    }

    measureParallelCountChange(ParallelCountChange.moneyThreshold(amount))
    measureParallelCountChange(ParallelCountChange.totalCoinsThreshold(coins.length))
    measureParallelCountChange(ParallelCountChange.combinedThreshold(amount, coins))
  }
}

object ParallelCountChange {

  /** Returns the number of ways change can be made from the specified list of
   *  coins for the specified amount of money.
   */
  def countChange(money: Int, coins: List[Int]): Int =
    if (money == 0) 1
    else if (money < 0) 0
    else
      coins match {
        case Nil =>  0
        case coin :: remainingCoins =>
          countChange(money - coin, coins) + countChange(money, remainingCoins)
      }

  type Threshold = (Int, List[Int]) => Boolean

  /** In parallel, counts the number of ways change can be made from the
   *  specified list of coins for the specified amount of money.
   */
  def parCountChange(money: Int, coins: List[Int], threshold: Threshold): Int =
    if (money == 0) 1
    else if (money < 0) 0
    else
      if(threshold(money,coins))
        countChange(money, coins)
      else
        coins match {
          case Nil => 0
          case coin :: remainingCoins => {
            val counts = parallel(parCountChange(money - coin, coins, threshold), parCountChange(money, remainingCoins, threshold))
            counts._1 + counts._2
          }
        }

  // > runMain reductions.ParallelCountChangeRunner
  /** Threshold heuristic based on the starting money.
    * -> speedup = 2.538 */
  def moneyThreshold(startingMoney: Int): Threshold = (currentMoney: Int, _) =>
    currentMoney <= ((startingMoney << 1) / 3.0)


  /** Threshold heuristic based on the total number of initial coins.
    * -> speedup = 3.498 */
  def totalCoinsThreshold(totalCoins: Int): Threshold = (_, currentCoins: List[Int]) =>
    currentCoins.length <= ((totalCoins << 1) / 3.0)


  /** Threshold heuristic based on the starting money and the initial list of coins.
    * -> speedup = 3.552 */
  def combinedThreshold(startingMoney: Int, allCoins: List[Int]): Threshold = (currentMoney: Int, currentCoins: List[Int]) =>
    currentMoney*currentCoins.length <= ((startingMoney * allCoins.length) >> 1)
}
