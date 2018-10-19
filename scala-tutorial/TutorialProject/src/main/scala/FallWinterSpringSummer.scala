import ChecksumAccumulator.calculate

/*
The way this works is that trait "App" declares a main method
of the appropriate signature, which your singleton object inherits, making it
usable as a Scala application. The code between the curly braces is collected
into a primary constructor of the singleton object, and is executed when the
class is initialized
=> DO NOT USE: Cannot take input args and only suitable for single threaded applications...
*/
object FallWinterSpringSummer extends App {
  for (season <- List("fall", "winter", "spring"))
    println(season +": "+ calculate(season))
}