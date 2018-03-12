package services

import metrics.PowerMetrics
import metrics.PowerMetrics._
import models.{Activity, ActivityStats, PowerEffort, UserSettings}
import modules.NonBlockingContext
import repositories.{ActivityStatsStore, PowerEffortStore, StravaStreamStore}

import com.typesafe.scalalogging.LazyLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PowerAnalysisService @Inject()(
  userService: UserService,
  streamStore: StravaStreamStore,
  effortStore: PowerEffortStore,
  activityStatsStore: ActivityStatsStore
)(implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

  def calculatePowerEfforts(activity: Activity): Seq[PowerEffort] = {
    val (times, wattsData, hrData) = streamStore.findByActivityId(activity.id).map { stream =>
      (stream.timeIndexInSeconds, stream.watts.getOrElse(0), stream.heartRate.getOrElse(0))
    }.unzip3

    for (interval <- 1 to times.last) yield calculatePowerEffort(activity, interval, wattsData, hrData)
  }

  def savePowerEfforts(efforts: Seq[PowerEffort]): Unit = efforts foreach effortStore.insert

  def getEffortsByActivityId(activityId: String): Seq[PowerEffort] = {
    effortStore.findByActivityId(activityId)
  }

  def createActivityStats(activity: Activity): Future[Option[ActivityStats]] = {
    userService.getSettingsFor(activity.startedAt) map {
      case Some(settings) =>
        Some(calculateActivityStats(activity, settings))
      case None =>
        logger.warn(s"No user settings found to support power stats for activity ${activity.id}")
        None
    }
  }

  def saveActivityStats(stats: ActivityStats): Unit = activityStatsStore.insert(stats)

  def longestEffort(activity: Activity): PowerEffort = {
    val efforts = getEffortsByActivityId(activity.id)
    efforts.sortWith(_.intervalLengthInSeconds > _.intervalLengthInSeconds).head
  }

  private def calculatePowerEffort(
    activity: Activity,
    length: Int,
    powerData: Seq[Int],
    hrData: Seq[Int]
  ): PowerEffort = {
    val interval = Interval(length, powerData, hrData)
    PowerEffort.create(
      activity,
      interval.lengthInSeconds,
      interval.startSecond,
      interval.avgHeartRate,
      interval.criticalPower,
      interval.normalizedPower
    )
  }

  private def calculateActivityStats(activity: Activity, settings: UserSettings): ActivityStats = {
    val effort = longestEffort(activity) // the entire activity
    val np = effort.normalizedPower.getOrElse(0)
    val intensity = PowerMetrics.intensityFactor(np, settings.ftp)
    val tss = PowerMetrics.stressScore(effort.intervalLengthInSeconds, np, settings.ftp, intensity)
    val vi = PowerMetrics.variabilityIndex(np, effort.criticalPower)
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

object Interval {
  def apply(length: Int, powerData: Seq[Int], hrData: Seq[Int]): Interval = {
    val result = maxAverageWithIndex(powerData, length)
    val from = result._2
    val to = from + length
    val start = result._2 + 1 // index is zero based but we don't do zero length intervals, right?
    val cp = result._1
    val hr = hrData.slice(from, to).sum / length
    val np = normalizedPower(powerData.slice(from, to))
    new Interval(length, start, cp, hr, np)
  }
}
