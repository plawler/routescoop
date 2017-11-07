package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import metrics.PowerMetricsUtils._
import models.{Activity, PowerEffort, PowerEffortsCreated}
import repositories.{PowerEffortStore, StravaStreamStore}


@Singleton
class PowerAnalysisService @Inject()(streamStore: StravaStreamStore, effortStore: PowerEffortStore) {
  
  def createEfforts(activity: Activity): Seq[PowerEffort] = {
    val (times, wattsData, hrData) = streamStore.findByActivityId(activity.id).map { stream =>
      (stream.timeIndexInSeconds, stream.watts.getOrElse(0), stream.heartRate.getOrElse(0))
    }.unzip3

    for (interval <- 1 to times.last) yield calculatePowerEffort(activity, interval, wattsData, hrData)
  }

  def saveEfforts(efforts: Seq[PowerEffort]): Unit = efforts foreach effortStore.insert

  def getEffortsByActivityId(activityId: String): Seq[PowerEffort] = {
    effortStore.findByActivityId(activityId)
  }

  private def calculatePowerEffort(activity: Activity, length: Int, powerData: Seq[Int], hrData: Seq[Int]): PowerEffort = {
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