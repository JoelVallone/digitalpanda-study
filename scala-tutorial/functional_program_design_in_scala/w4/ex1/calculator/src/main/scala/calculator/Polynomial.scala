package calculator

import scala.math.{pow, sqrt}

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = {
    Signal { pow(b(),2) - 4*a()*c() }
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] =
    Signal {
      if (a() != 0 && delta() >= 0)
        Set(
          (-b() + sqrt(delta())) / (2*a()),
          (-b() - sqrt(delta())) / (2*a()))
      else
        Set()
    }
}
