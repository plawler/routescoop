package models

import play.api.libs.json.Json

case class Effort(duration: Int, watts: Int)

object Effort {
  implicit val format = Json.format[Effort]
}

case class CriticalPower(cp: Double, wPrime: Int, predictions: Seq[Effort])

object CriticalPower {
  implicit val format = Json.format[CriticalPower]
}

sealed trait CriticalPowerResult
case class CriticalPowerResultSuccess(cp: CriticalPower) extends CriticalPowerResult
case class CriticalPowerResultError(message: String) extends CriticalPowerResult
