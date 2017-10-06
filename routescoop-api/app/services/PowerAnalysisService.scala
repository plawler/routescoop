package services

import java.time.Instant
import javax.inject.{Inject, Singleton}

import metrics.AnalysisUtils._
import metrics.Result
import models.Activity
import repositories.{PowerEffortStore, StravaStreamStore}


@Singleton
class PowerAnalysisService @Inject()(streamStore: StravaStreamStore, effortStore: PowerEffortStore) {

  def createEfforts(activity: Activity): Seq[PowerEffort] = {
    val (intervals, wattsData, hrData) = streamStore.findByActivityId(activity.id).map { stream =>
      (stream.timeIndexInSeconds, stream.watts.getOrElse(0), stream.heartRate.getOrElse(0))
    }.unzip3
    intervals map (interval => calculatePowerEffort(activity, interval, wattsData, hrData))
  }

  def saveEfforts(efforts: Seq[PowerEffort]): Unit = efforts foreach effortStore.insert

  private def calculatePowerEffort(activity: Activity, interval: Int, powerData: Seq[Int], hrData: Seq[Int]): PowerEffort = {
    val cpResult = maxAverageResult(powerData, interval)
    val avgHr = avgHeartRate(interval, hrData, cpResult)
    if (interval >= 30) {
      val np = normalizedPower(powerData).toInt
      PowerEffort(activity, interval, cpResult.startIndex, avgHr, cpResult.value, np)
    } else PowerEffort(activity, interval, cpResult.startIndex, avgHr, cpResult.value)
  }

  private def avgHeartRate(interval: Int, hrData: Seq[Int], maxAvg: Result): Int = {
    val from = maxAvg.startIndex - 1
    val to = from + interval
    hrData.slice(from, to).sum / interval
  }

}

case class PowerEffort(
  activityId: String,
  intervalLengthInSeconds: Int,
  startedAt: Instant,
  avgHeartRate: Int,
  criticalPower: Int,
  normalizedPower: Option[Int] = None // np is not valuable in lower time ranges
)

object PowerEffort {

  def apply(
    activity: Activity,
    intervalLengthInSeconds: Int,
    startAtSecond: Int,
    avgHeartRate: Int,
    criticalPower: Int
  ): PowerEffort =
    new PowerEffort(
      activity.id,
      intervalLengthInSeconds,
      activity.startedAt.plusSeconds(startAtSecond),
      avgHeartRate,
      criticalPower
    )

  def apply(
    activity: Activity,
    intervalLengthInSeconds: Int,
    startAtSecond: Int,
    avgHeartRate: Int,
    criticalPower: Int,
    normalizedPower: Int
  ): PowerEffort =
    new PowerEffort(
      activity.id,
      intervalLengthInSeconds,
      activity.startedAt.plusSeconds(startAtSecond),
      avgHeartRate,
      criticalPower,
      Some(normalizedPower)
    )

}