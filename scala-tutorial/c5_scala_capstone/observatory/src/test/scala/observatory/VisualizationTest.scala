package observatory


import observatory.Main.sc
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
trait VisualizationTest extends FunSuite with Checkers with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    sc.stop()
  }

  //TODO: Tests...
}
