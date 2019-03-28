import problem.Pouring

object Test {
  println("Inside test object")
  val problem = new Pouring(Vector(1, 2))
  println(problem.solutions(1) take 2 toList)
}

Test
println("Hello World")
3
4

def from(n: Int): Stream[Int] = n #:: from(n+1)

from(2) take (10) toList




