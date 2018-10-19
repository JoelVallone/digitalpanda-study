

class ChecksumAccumulator {
  // class definition goes here.. Inside a class definition, you place fields and methods, which are collectively called members.

  //Field is an instance variable, default access level is public
  private var sum = 0

  //Method parameters are val by default (final in java)
  def add(b: Byte) { sum += b } // Procedure like braces for unit function => no "=" symbol required
  def checksum(): Int = ~ (sum & 0xFF) + 1
}


/* SINGLETON OBJECTS
 => Classes in Scala cannot have static members.
 => Instead, Scala has singleton objects
When a singleton object shares the same name with a class, it is called that class’s companion object.
You must define both the class and its companion object in the same source
file. The class is called the companion class of the singleton object. A class
and its companion object can access each other’s private members.
 => Defining a singleton object doesn’t define a type (at the Scala level of abstraction).
 => Singleton objects extend a superclass and can mix in traits
 => Cannot be instantiated as they behave as a Java class
 => Only initialized when first referred to
 => The name of the synthetic class (compiler generated class) is the object name plus a dollar sign. Thus the synthetic
class for the singleton object named ChecksumAccumulator is ChecksumAccumulator$ .
*/
object ChecksumAccumulator {
  /*
    optimisation...might use a weak map, such as WeakHashMap in scala.collection.jcl , so
that entries in the cache could be garbage collected if memory becomes scarce.
   */
  private val cache = scala.collection.mutable.Map[String, Int]()

  def calculate(s: String): Int =
    if (cache.contains(s))
      cache(s)
    else {
      val acc = new ChecksumAccumulator
      for (c <- s)
        acc.add(c.toByte) //Can access the private fields of its "companion" class
      val cs = acc.checksum()
      cache += (s -> cs)
      cs
    }
}
