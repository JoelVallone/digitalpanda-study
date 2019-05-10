package stackoverflow

import org.apache.spark.rdd.RDD
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import stackoverflow.StackOverflow.sc

import scala.collection.Map

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {

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
    sc.stop()
  }

  lazy val uutBasic = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
    override def langSpread = 50000
    override def kmeansKernels = 45
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 120
  }

  val stackoverflowBasic = List(
    "1,1001,1002,,8000,C++",
    "2,1002,,1001,33,",
    "2,1003,,1001,1,",
    "1,2001,2002,,9000,Java",
    "2,2002,,2001,42,",
    "1,3001,,,3,Perl"
  )

  test("'scoredPostings' - correctness") {
    // Given
    val lines = sc.parallelize(stackoverflowBasic)

    // When
    val scores = processUntilScoredPostings(lines, uutBasic)
    val actual = scores.collectAsMap()

    // Then
    assert(actual.size === 2)
    assertScore(actual, Posting(1,1001,Some(1002),None,8000,Some("C++")), 33)
    assertScore(actual, Posting(1,2001,Some(2002),None,9000,Some("Java")), 42)
  }

  /*
    test("'scoredPostings' - scaling") {
      // Given
      val lines   = sc.textFile("src/main/resources/stackoverflow/stackoverflow.csv")

      // When
      val scores = processUntilScoredPostings(lines, uutBasic)
      val actual = scores.collectAsMap()

      // Then
      println("scoredPostings: " + actual)
      assertScore(actual, Posting(1, 6, None, None, 140, Some("CSS")), 67)
      assertScore(actual, Posting(1, 42, None, None, 155, Some("PHP")), 89)
      assertScore(actual, Posting(1, 72, None, None, 16, Some("Ruby")), 3)
      assertScore(actual, Posting(1, 126, None, None, 33, Some("Java")), 30)
      assertScore(actual, Posting(1, 174, None, None, 38, Some("C#")), 20)
    }
  */

  def assertScore(scores : Map[Question, HighScore], question : Question, score: Int): Unit = {
    assert(scores contains question)
    assert(scores(question) === score)
  }

  test("'vectorPostings'  - correctness") {
    // Given
    val lines = sc.parallelize(stackoverflowBasic)

    // When
    val vectors = processUntilVectoredPostings(lines, uutBasic)
    val actual = vectors.collectAsMap()

    // Then
    assert(actual.size === 2)
    assert(actual(250000) == 33)
    assert(actual(50000) == 42)
  }

  /*
  test("'vectorPostings' - scaling") {
    // Given
    val lines = sc.parallelize(stackoverflowBasic)

    // When
    val vectors = processUntilVectoredPostings(lines, uutBasic)
    val actual = vectors.count()

    // Then
    assert(actual == 2121822, "Incorrect number of vectors: " + actual)
  }
  */

  lazy val uutClustering = new StackOverflow {
    override val langs = List("Java", "C++")
    override def langSpread = 50000
    override def kmeansKernels = 2
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 1
  }

  test("'cluster' creates the wished scorings - correctness") {
    // Given
    val stackoverflowCluster = List(

      "1,1001,1002,,8000,C++",
      "2,1002,,1001,33,",

      "1,2001,2002,,9000,Java",
      "2,2002,,2001,10,",
      "1,2011,2012,,9000,Java",
      "2,2012,,2011,20,"
    )
    val uut = uutClustering
    import uut._
    val lines = sc.parallelize(stackoverflowCluster)
    val vectors = processUntilVectoredPostings(lines, uut)
    val initMeans = sampleVectors(vectors).reverse

    // When
    val means   = kmeans(initMeans, vectors, debug = true)

    /// Then
    println(s"initMeans=${initMeans.mkString(",")}")
    println(s"means=${means.mkString(",")}")
    assert(initMeans.length == means.length)
    assert(initMeans.map(_._1) === means.map(_._1))
    assert(means(0) === (0, 15))
    assert(means(1) === (50000, 33))
  }

  def processUntilVectoredPostings(lines : RDD[String], uut : StackOverflow): RDD[(LangIndex, HighScore)] = {
    uut.vectorPostings(processUntilScoredPostings(lines, uut))
  }

  def processUntilScoredPostings(lines : RDD[String], uut: StackOverflow): RDD[(Question, HighScore)] = {
    val raw     = uut.rawPostings(lines)
    val grouped = uut.groupedPostings(raw)
    uut.scoredPostings(grouped)
  }
}
