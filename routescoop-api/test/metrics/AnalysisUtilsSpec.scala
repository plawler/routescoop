package metrics

import org.scalatest.{FlatSpec, Matchers}
import AnalysisUtils._

class AnalysisUtilsSpec extends FlatSpec with Matchers {

  "Power metrics" should "calculate mean max power" in {
    for {
      i <- 1 to 11
    } yield {
      maxAverage(Seq.fill(11)(7), i) shouldEqual 7
    }
  }

  it should "handle zero interval" in {
    intercept[IllegalArgumentException] {
      maxAverage(Seq.fill(11)(7), 0) shouldEqual 7
    }
  }

  it should "calculate a rolling average" in {
    val data = Seq(100, 150, 200, 100, 150, 150, 150, 200, 100, 200)
    val rolled = rollingAverage(data, 3)
    rolled shouldBe Seq(150, 150, 150, 134, 151, 167, 150, 166)
  }

  it should "calculate normalized power" in {
    val data = 1 to 1000 map (n => 200)
    normalizedPower(data) shouldEqual 200
  }

}

case class IndexedWatts(startingAtSecond: Int, watts: Int)