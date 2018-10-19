/*
  To run a Scala program, you must supply the name of a standalone singleton
  object with a main method that takes one parameter, an Array[String] ,
  and has a result type of Unit. Any standalone object with a main method of
  the proper signature can be used as the entry point into an application
  1) scalac ChecksumAccumulator.scala Summer.scala // or "fsc" command
  2) scala Summer of love
   */
object Summer {
  def main(args: Array[String]) {
    for (arg <- args)
      println(arg +": "+ ChecksumAccumulator.calculate(arg))
  }
}
