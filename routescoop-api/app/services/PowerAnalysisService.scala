package services

import javax.inject.{Inject, Singleton}
import metrics.PowerMetrics._
import models.{Activity, ActivityStats, Interval, PowerEffort, PowerEffortsCreated, UserSettings}
import modules.NonBlockingContext
import repositories.{ActivityStatsStore, PowerEffortStore, StravaStreamStore, UserSettingsStore}
import utils.IntervalUtils

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PowerAnalysisService @Inject()(
  activityService: StravaActivityService,
  userSettingsStore: UserSettingsStore,
  streamStore: StravaStreamStore,
  effortStore: PowerEffortStore,
  activityStatsStore: ActivityStatsStore
)(implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

//  def createPowerEfforts(dataSyncId: String): Future[Unit] = {
//    activityService.getActivitiesBySync(dataSyncId) map { activities =>
//      activities.foreach { activity =>
//        val efforts = calculatePowerEfforts(activity)
//        savePowerEfforts(efforts)
//        actorSystem.eventStream.publish(PowerEffortsCreated(activity))
//      }
//    }
//  }

  def calculatePowerEfforts(activity: Activity): Seq[PowerEffort] = {
    val (times, watts, heartRates) = streamStore.findByActivityId(activity.id).map { stream =>
      (stream.timeIndexInSeconds, stream.watts.getOrElse(0), stream.heartRate.getOrElse(0))
    }.unzip3
    // the timeIndexInSeconds is not consecutive due to activity pauses
    // however i still need to capture the timeIndexInSeconds to get the overall stats correct
    buildIntervalIndices(times) map (interval => calculatePowerEffort(activity, interval, times, watts, heartRates))
  }

  def savePowerEfforts(efforts: Seq[PowerEffort]): Unit = efforts foreach effortStore.insert

  def getEffortsByActivityId(activityId: String): Seq[PowerEffort] = {
    effortStore.findByActivityId(activityId)
  }

  def createActivityStats(activity: Activity): Future[Option[ActivityStats]] = Future {
    getSettingsFor(activity) match {
      case Some(settings) =>
        Some(calculateActivityStats(activity, settings))
      case None =>
        logger.warn(s"No user settings found to support power stats for activity ${activity.id}")
        None
    }
  }

  def recalculateActivityStats(newSettings: UserSettings): Future[Unit] = {
    val userId = newSettings.userId
    val start = newSettings.createdAt
    val end = (getEarliestSettingsAfter(newSettings.createdAt, userId) map (_.createdAt)) getOrElse Instant.now
    activityService.findBetween(start, end, userId) map { activities =>
      activities.foreach { activity =>
        updateActivityStats(calculateActivityStats(activity, newSettings))
      }
    }
  }

  def saveActivityStats(stats: ActivityStats): Unit = {
    logger.info(s"Saving the activity stats $stats")
    activityStatsStore.insert(stats)
  }

  def updateActivityStats(stats: ActivityStats): Unit = {
    activityStatsStore.findByActivityId(stats.activityId) match {
      case Some(_) =>
        logger.info(s"updating the activity stats $stats")
        activityStatsStore.update(stats)
      case None => saveActivityStats(stats)
    }
  }

  def longestEffort(activity: Activity): PowerEffort = {
    val efforts = getEffortsByActivityId(activity.id)
    efforts.sortWith(_.intervalLengthInSeconds > _.intervalLengthInSeconds).head
  }

  def getEarliestSettingsAfter(timestamp: Instant, userId: String): Option[UserSettings] = {
    userSettingsStore.findEarliestAfter(timestamp, userId)
  }

  def getNextSettingsAfter(settings: UserSettings): Option[UserSettings] = {
    val userId = settings.userId
    val start = settings.createdAt
    getEarliestSettingsAfter(start, userId)
  }

  private def getSettingsFor(activity: Activity): Option[UserSettings] = {
    userSettingsStore.findLatestUntil(activity.startedAt, activity.userId)
      .orElse(userSettingsStore.findEarliestAfter(activity.startedAt, activity.userId))
  }

  private def buildIntervalIndices(times: Seq[Int]): List[Int] = IntervalUtils.calculateProcessingIntervals(times.max)

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
    Interval(length, start, cp, hr, np)
  }

  def calculateActivityStats(activity: Activity, settings: UserSettings): ActivityStats = {
    val effort = longestEffort(activity) // the entire activity
    val np = effort.normalizedPower.getOrElse(0)
    val intensity = intensityFactor(np, settings.ftp)
    val tss = stressScore(effort.intervalLengthInSeconds, np, settings.ftp, intensity)
    val vi = variabilityIndex(np, effort.criticalPower)
    ActivityStats(effort.activityId, settings.id, effort.criticalPower, np, tss, intensity, vi)
  }

}




