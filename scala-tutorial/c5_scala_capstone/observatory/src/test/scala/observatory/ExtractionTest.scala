package observatory

import java.time.LocalDate

import observatory.Main.sc
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

@RunWith(classOf[JUnitRunner])
trait ExtractionTest extends FunSuite  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    sc.stop()
  }

  test("'locateTemperatures' - correctness") {
    // Given
    val expected = Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.299999999999997),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.000000000000001)
    )

    // When
    val actual = Extraction.locateTemperatures(
      2015,
      "/stations-test.csv",
      "/2015-test.csv")

    // Then
    assert(actual.toSet === expected.toSet)
  }

  test("'locationYearlyAverageRecords' - correctness") {
    // Given
    val measures = Seq(
      (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
      (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
      (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.000000000000001)
    )
    val expected = Seq(
      (Location(37.35, -78.433), 27.3),
      (Location(37.358, -78.438), 1.0000000000000004)
    )

    // When
    val actual = Extraction.locationYearlyAverageRecords(measures)

    // Then
    assert(actual.toSet === expected.toSet)
  }
}