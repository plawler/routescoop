package models

import play.api.libs.json.Json

import java.time.{Instant, LocalDate}

case class Effort(
  duration: Int,
  watts: Int,
  heartRate: Option[Int] = None,
  startedAt: Option[Instant] = None
)

case class ChartedEffort(
  duration: String,
  watts: Int,
  heartRate: Option[Int] = None,
  date: Option[LocalDate] = None
)

object Effort {
  implicit val format = Json.format[Effort]
}

case class CriticalPower(cp: Double, wPrime: Int, predictions: Seq[Effort])

object CriticalPower {
  implicit val format = Json.format[CriticalPower]
}

case class MeanMaxPower(efforts: Seq[Effort])

object MeanMaxPower {
  implicit val format = Json.format[MeanMaxPower]
}

case class PowerProfile(cp: CriticalPower, mmp: MeanMaxPower)

object PowerProfile {
  implicit val format = Json.format[PowerProfile]
}

sealed trait CriticalPowerResult
case class CriticalPowerResultSuccess(cp: CriticalPower) extends CriticalPowerResult
case class CriticalPowerResultError(message: String) extends CriticalPowerResult

sealed trait MeanMaxPowerResult
case class MeanMaxPowerResultSuccess(mmp: MeanMaxPower) extends MeanMaxPowerResult
case class MeanMaxPowerResultError(message: String) extends MeanMaxPowerResult

sealed trait PowerProfileResult
case class PowerProfileResultSuccess(pp: PowerProfile) extends PowerProfileResult
case class PowerProfileResultError(message: String) extends PowerProfileResult
