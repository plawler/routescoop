package metrics

import org.scalatest.{FlatSpec, Matchers}
import PowerMetricsUtils._

class PowerMetricsUtilsSpec extends FlatSpec with Matchers with Fixture {

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
    val rolled = rollingAverage(data, interval)
    rolled shouldBe Seq(150, 150, 150, 134, 151, 167, 150, 166)
  }

  it should "calculate the max value with index in a rolling average" in {
    val data = Seq(100, 150, 200, 100, 150, 150, 150, 200, 100, 200)
    maxAverageWithIndex(data, interval) shouldEqual (167, 5)
  }

  it should "calculate normalized power" in {
    val data = 1 to 1000 map (n => 200)
    normalizedPower(data) shouldEqual Some(200)
    normalizedPower(data.take(29)) shouldBe None
  }
}

trait Fixture {
  val data = Seq(100, 150, 200, 100, 150, 150, 150, 200, 100, 200)
  val interval = 3
}