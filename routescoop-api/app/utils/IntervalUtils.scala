package utils

import scala.collection.SortedSet

object IntervalUtils {

  private val OneSecond = 1
  private val FiveMinutes = 60 * 5
  private val ThirtyMinutes = 60 * 30
  private val OneHour = 60 * 60
  private val TwoHours = OneHour * 2

  val PowerProcessingSteps = Map(OneSecond -> 1, FiveMinutes -> 1, ThirtyMinutes -> 5, OneHour -> 30)
  val TrainerRoadDisplaySteps = Map(OneSecond -> 1, FiveMinutes -> 5, ThirtyMinutes -> 30, OneHour -> 30, TwoHours -> 300)

  def buildIntervalIndices(totalSeconds: Int): List[Int] = {
    val initialSteps = PowerProcessingSteps
    // each range is where we change intervals that we track a power effort for
    val ranges = SortedSet(OneSecond, FiveMinutes, ThirtyMinutes, OneHour).filter(_ <= totalSeconds)
    // increments depend on the interval range we are in
    //val initialSteps = Map(OneSecond -> 1, FiveMinutes -> 1, ThirtyMinutes -> 5, OneHour -> 30)
    // figure out the increment for the totalTime range
    val steps = initialSteps.get(ranges.max) match {
      case Some(increment) => initialSteps + (totalSeconds -> increment)
      case None => initialSteps + (totalSeconds -> 60)
    }

    val ranges2 = (ranges + totalSeconds).toSeq // assumes we never have a 1 second workout ;)

    ranges2.sliding(2).toList flatMap { pair => // use the sliding function to get pairs of previous and next ranges
      val previousRange = pair.head
      val nextRange = pair.last
      if (nextRange == totalSeconds) {
        previousRange to nextRange by steps(nextRange) // the last range we include the final value
      } else {
        previousRange until nextRange by steps(nextRange) // the last value of the previous range will be the first value of the next range
      }
    }
  }

}
