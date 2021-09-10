package example

import example.Lists.*

object main {

  @main def run(): Unit =
    println(s"max(List(1,3,2)) = ${max(List(1,3,2))}")
    println(s"sum(List(1,3,2)) = ${sum(List(1,3,2))}")

}
