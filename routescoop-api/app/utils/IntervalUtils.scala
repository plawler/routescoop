package utils

import scala.collection.SortedSet

object IntervalUtils {

  private val OneSecondInterval = 1
  private val FiveMinuteInterval = 60 * 5
  private val ThirtyMinutesInterval = 60 * 30
  private val OneHourInterval = 60 * 60
  private val TwoHourInterval = OneHourInterval * 2

  val PowerProcessingSteps: Map[Int, Int] = Map(OneSecondInterval -> 1, FiveMinuteInterval -> 1, ThirtyMinutesInterval -> 5, OneHourInterval -> 30)
  val DisplaySteps = Map(OneSecondInterval -> 1, FiveMinuteInterval -> 5, ThirtyMinutesInterval -> 30, OneHourInterval -> 30, TwoHourInterval -> 300)

  def buildIntervalIndices(totalDurationInSeconds: Int, initialSteps: Map[Int, Int]): List[Int] = {
    // each range is where we change intervals that we track a power effort for
    val ranges = SortedSet(initialSteps.keys.toList: _*).filter(_ <= totalDurationInSeconds)

    // increments depend on the interval range we are in
    // figure out the increment for the totalTime range
    val steps = initialSteps.get(ranges.max) match {
      case Some(increment) => initialSteps + (totalDurationInSeconds -> increment)
      case None => initialSteps + (totalDurationInSeconds -> 60)
    }

    val ranges2 = (ranges + totalDurationInSeconds).toSeq // assumes we never have a 1 second workout ;)
    ranges2.sliding(2).toList flatMap { pair => // use the sliding function to get pairs of previous and next ranges
      val previousRange = pair.head
      val nextRange = pair.last
      if (nextRange == totalDurationInSeconds) {
        previousRange to nextRange by steps(nextRange) // the last range we include the final value
      } else {
        previousRange until nextRange by steps(nextRange) // the last value of the previous range will be the first value of the next range
      }
    }
  }

  def calculateProcessingIntervals(totalDurationInSeconds: Int): List[Int] = {
    buildIntervalIndices(totalDurationInSeconds, PowerProcessingSteps)
  }

  def calculateDisplayIntervals(totalDurationInSeconds: Int): List[Int] =
    calculateProcessingIntervals(totalDurationInSeconds)

}
