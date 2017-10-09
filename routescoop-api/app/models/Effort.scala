package models

import java.time.Instant

import anorm.{Macro, RowParser}

sealed trait Effort

case class PowerEffort(
  activityId: String,
  intervalLengthInSeconds: Int,
  startedAt: Instant,
  avgHeartRate: Int,
  criticalPower: Int,
  normalizedPower: Option[Int] = None // np is not valuable in lower time ranges
) extends Effort

object PowerEffort {

  implicit val parser: RowParser[PowerEffort] = Macro.namedParser[PowerEffort]

  def create(
    activity: Activity,
    intervalLengthInSeconds: Int,
    startAtSecond: Int,
    avgHeartRate: Int,
    criticalPower: Int,
    normalizedPower: Option[Int]
  ): PowerEffort =
    new PowerEffort(
      activity.id,
      intervalLengthInSeconds,
      activity.startedAt.plusSeconds(startAtSecond),
      avgHeartRate,
      criticalPower,
      normalizedPower
    )

}
