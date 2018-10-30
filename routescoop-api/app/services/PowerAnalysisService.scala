package services

import metrics.PowerMetrics
import metrics.PowerMetrics._
import models.{Activity, ActivityStats, PowerEffort, UserSettings}
import modules.NonBlockingContext
import repositories.{ActivityStatsStore, PowerEffortStore, StravaStreamStore}
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}

import scala.collection.SortedSet
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PowerAnalysisService @Inject()(
  userService: UserService,
  streamStore: StravaStreamStore,
  effortStore: PowerEffortStore,
  activityStatsStore: ActivityStatsStore
)(implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

  def calculatePowerEfforts(activity: Activity): Seq[PowerEffort] = {
    val (times, watts, heartRates) = streamStore.findByActivityId(activity.id).map { stream =>
      (stream.timeIndexInSeconds, stream.watts.getOrElse(0), stream.heartRate.getOrElse(0))
    }.unzip3

    // the timeIndexInSeconds is not consecutive due to activity pauses
    // however i still need to capture the timeIndexInSeconds to get the overall stats correct
    buildIntervalIndices(times) map (interval => calculatePowerEffort(activity, interval, times, watts, heartRates))
  }

  def buildIntervalIndices(times: Seq[Int]) = {
    val oneSecond = 1
    val fiveMinutes = 60 * 5
    val thirtyMinutes = 60 * 30
    val oneHour = 60 * 60
    val totalTime = times.size

    // each range is where we change intervals that we track a power effort for
    val ranges = SortedSet(oneSecond, fiveMinutes, thirtyMinutes, oneHour).filter(_ <= totalTime)
    // increments depend on the interval range we are in
    val initialSteps = Map(oneSecond -> 1, fiveMinutes -> 1, thirtyMinutes -> 5, oneHour -> 30)
    // figure out the increment for the totalTime range
    val steps = initialSteps.get(ranges.max) match {
      case Some(increment) => initialSteps + (totalTime -> increment)
      case None => initialSteps + (totalTime -> 60)
    }

    val ranges2 = (ranges + totalTime).toSeq // assumes we never have a 1 second workout ;)

    ranges2.sliding(2).toList flatMap { pair => // use the sliding function to get pairs of previous and next ranges
      val previousRange = pair.head
      val nextRange = pair(1)
      if (nextRange == totalTime) {
        previousRange to nextRange by steps(nextRange) // the last range we include the final value
      } else {
        previousRange until nextRange by steps(nextRange) // the last value of the previous range will be the first value of the next range
      }
    }
  }

  def savePowerEfforts(efforts: Seq[PowerEffort]): Unit = efforts foreach effortStore.insert

  def getEffortsByActivityId(activityId: String): Seq[PowerEffort] = {
    effortStore.findByActivityId(activityId)
  }

  def createActivityStats(activity: Activity): Future[Option[ActivityStats]] = {
    userService.getSettingsFor(activity) map {
      case Some(settings) =>
        Some(calculateActivityStats(activity, settings))
      case None =>
        logger.warn(s"No user settings found to support power stats for activity ${activity.id}")
        None
    }
  }

  def saveActivityStats(stats: ActivityStats): Unit = {
    logger.info(s"Saving the activity stats $stats")
    activityStatsStore.insert(stats)
  }

  def longestEffort(activity: Activity): PowerEffort = {
    val efforts = getEffortsByActivityId(activity.id)
    efforts.sortWith(_.intervalLengthInSeconds > _.intervalLengthInSeconds).head
  }

  private def calculatePowerEffort(
    activity: Activity,
    length: Int, // the index of the value in the stream
    times: Seq[Int], // the second of the ride recorded to the stream
    powerData: Seq[Int],
    hrData: Seq[Int]
  ): PowerEffort = {
    val interval = calculateInterval(length, times, powerData, hrData)
    PowerEffort.create(
      activity,
      length,
      interval.startSecond,
      interval.avgHeartRate,
      interval.criticalPower,
      interval.normalizedPower
    )
  }

  private def calculateInterval(length: Int, times: Seq[Int], powerData: Seq[Int], hrData: Seq[Int]): Interval = {
    val result = maxAverageWithIndex(powerData, length)
    val from = result._2
    val to = from + length
    val start = times(from)
    val cp = result._1
    val hr = hrData.slice(from, to).sum / length
    val np = normalizedPower(powerData.slice(from, to))
    new Interval(length, start, cp, hr, np)
  }

  private def calculateActivityStats(activity: Activity, settings: UserSettings): ActivityStats = {
    val effort = longestEffort(activity) // the entire activity
    val np = effort.normalizedPower.getOrElse(0)
    val intensity = intensityFactor(np, settings.ftp)
    val tss = stressScore(effort.intervalLengthInSeconds, np, settings.ftp, intensity)
    val vi = variabilityIndex(np, effort.criticalPower)
    ActivityStats(effort.activityId, settings.id, effort.criticalPower, np, tss, intensity, vi)
  }

}

case class Interval(
  lengthInSeconds: Int,
  startSecond: Int, // the second in the stream that calculated maximum effort was found
  criticalPower: Int,
  avgHeartRate: Int,
  normalizedPower: Option[Int]
)


