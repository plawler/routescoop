package models

import java.time.Instant
import anorm.{Macro, RowParser}

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait Effort {
  val duration: Int
  val startedAt: Instant
  val watts: Int
  val heartRate: Int
}

object Effort {
  implicit val writes = new Writes[Effort] {
    override def writes(effort: Effort): JsValue = {
      effort match {
        case pe: PowerEffort =>
          Json.obj(
            "startedAt" -> pe.startedAt,
            "duration" -> pe.duration,
            "watts" -> pe.watts,
            "heartRate" -> pe.heartRate
          )
      }
    }
  }
}

case class PowerEffort(
  activityId: String,
  intervalLengthInSeconds: Int,
  startedAt: Instant,
  avgHeartRate: Int,
  criticalPower: Int,
  normalizedPower: Option[Int] = None // np is not valuable in lower time ranges
) extends Effort {
  val duration: Int = intervalLengthInSeconds
  val watts: Int = criticalPower // this was a misunderstanding on my part. field should be 'watts'
  val heartRate: Int = avgHeartRate
}

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

case class PowerEffortsCreated(activity: Activity, createdAt: Instant = Instant.now)
