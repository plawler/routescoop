package utils

import org.scalatest.{FreeSpec, Matchers}
import utils.IntervalUtils._

class IntervalUtilsSpec extends FreeSpec with Matchers {

  "it should build the indices for the intervals we track" in {
    buildIntervalIndices(14440)
  }

  "it should build the interval indices for power efforts" in {
    buildIntervalIndices(30) should have size 30
    buildIntervalIndices(300) should have size 300
    buildIntervalIndices(1800) should have size 600
    buildIntervalIndices(5400) should have size 720
    buildIntervalIndices(300)(299) shouldEqual 300
    buildIntervalIndices(1800)(599) shouldEqual 1800
  }

}
