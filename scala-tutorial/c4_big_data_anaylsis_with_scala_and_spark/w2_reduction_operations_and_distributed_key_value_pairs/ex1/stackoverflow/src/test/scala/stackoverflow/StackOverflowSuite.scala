package stackoverflow

import org.apache.spark.rdd.RDD
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.Map

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {
/*
  def initializeStackOverflow(): Boolean =
    try {
      StackOverflow
      true
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        ex.printStackTrace()
        false
    }

  override def afterAll(): Unit = {
    assert(initializeStackOverflow(), " -- did you fill in all the values in WikipediaRanking (conf, sc, wikiRdd)?")
    import StackOverflow._
    sc.stop()
  }
*/

  lazy val testObject = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
    override def langSpread = 50000
    override def kmeansKernels = 45
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 120
  }

  test("testObject can be instantiated") {
    val instantiatable = try {
      testObject
      true
    } catch {
      case _: Throwable => false
    }
    assert(instantiatable, "Can't instantiate a StackOverflow object")
  }
/*

  test("'scoredPostings' creates the wished scorings - correctness") {
    import StackOverflow._
    testObject
    val stackoverflow = List(
      "1,1001,1002,,8000,C++",
      "2,1002,,1001,33,",
      "2,1003,,1001,1,",
      "1,2001,2002,,9000,Java",
      "2,2002,,2001,42,",
      "1,3001,,,3,Perl"
    )
    val lines = sc.parallelize(stackoverflow)
    val scores = processUntilScoredPostings(lines)
    val map = scores.collectAsMap()
    assert(map.size === 2)
    assertScore(map, Posting(1,1001,Some(1002),None,8000,Some("C++")), 33)
    assertScore(map, Posting(1,2001,Some(2002),None,9000,Some("Java")), 42)

  }*/

/*
  test("'scoredPostings' creates the wished scorings - scaling") {

    import StackOverflow._
    testObject
    val lines   = sc.textFile("src/main/resources/stackoverflow/stackoverflow.csv")
    val scores = processUntilScoredPostings(lines)
    val map = scores.collectAsMap()
    println("scoredPostings: " + map)
    assertScore(map, Posting(1, 6, None, None, 140, Some("CSS")), 67)
    assertScore(map, Posting(1, 42, None, None, 155, Some("PHP")), 89)
    assertScore(map, Posting(1, 72, None, None, 16, Some("Ruby")), 3)
    assertScore(map, Posting(1, 126, None, None, 33, Some("Java")), 30)
    assertScore(map, Posting(1, 174, None, None, 38, Some("C#")), 20)

  }
*/
  def assertScore(scores : Map[Question, HighScore], question : Question, score: Int): Unit = {
    assert(scores contains question)
    assert(scores(question) === score)
  }

  def processUntilScoredPostings(lines : RDD[String]): RDD[(Question, HighScore)] = {
    import StackOverflow._
    val raw     = rawPostings(lines)
    val grouped = groupedPostings(raw)
    println("groupedPostings: " + grouped.collectAsMap())
    scoredPostings(grouped)
  }



}
