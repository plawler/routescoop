package utils

import org.scalatest.{FreeSpec, Ignore, Matchers}
import utils.IntervalUtils._

class IntervalUtilsSpec extends FreeSpec with Matchers {

  "it should build the indices for the intervals we track" in {
    val durationInSeconds = 14440
    val maxProcessedIntervalDuration = 14430 // on the 30 second step
    calculateProcessingIntervals(durationInSeconds).max shouldEqual maxProcessedIntervalDuration
  }

  "it should build the interval indices for power efforts" in {
    calculateProcessingIntervals(30) should have size 30
    calculateProcessingIntervals(300) should have size 300
    calculateProcessingIntervals(1800) should have size 600
    calculateProcessingIntervals(5400) should have size 720
    calculateProcessingIntervals(300)(299) shouldEqual 300
    calculateProcessingIntervals(1800)(599) shouldEqual 1800
  }

}
