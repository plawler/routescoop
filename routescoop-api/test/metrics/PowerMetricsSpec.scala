package metrics

import fixtures.StressScoreFixture
import metrics.PowerMetrics._

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class PowerMetricsSpec extends FlatSpec with Matchers with Fixture {

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
    val rolled = rollingAverage(data, interval) map (_.toInt)
    rolled shouldBe Seq(150, 150, 150, 133, 149, 166, 149, 166)

  }

  it should "calculate the max value with index in a rolling average" in {
    maxAverageWithIndex(data, interval) shouldEqual (166, 5)
  }

  it should "calculate normalized power" in {
    val data = 1 to 1000 map (n => 200)
    normalizedPower(data) shouldEqual Some(200)
    normalizedPower(data.take(29)) shouldBe None
  }

  it should "calculate power metrics with real data" in {
    val watts = Thread.currentThread.getContextClassLoader.getResourceAsStream("watts.txt")
    val line = Source.fromInputStream(watts).getLines()
    val data = line.mkString.split(",").toList
    movingAverage(data.map(_.toDouble), 60 * 30).max shouldEqual 189.13888888888818
    rollingAverage(data.map(_.toInt), 60 * 30).max shouldEqual 189.13888888888818
  }

  it should "calculate chronic training load (fitness)" in {
    trainingLoad(startingCtl, stressScores.head, 42) shouldEqual 52.7
  }

  it should "calculate acute training load (fatigue)" in {
    val startingAtl = 65
    trainingLoad(startingAtl, stressScores.head, 7) shouldEqual 67.6
  }

}

trait Fixture extends StressScoreFixture {
  val data = Seq(100, 150, 200, 100, 150, 150, 150, 200, 100, 200)
  val interval = 3
}
