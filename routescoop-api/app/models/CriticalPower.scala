package models

import play.api.libs.json.Json

case class CriticalPowerPrediction(
  duration: Int,
  watts: Int
)

case class CriticalPower(
  cp: Double,
  wPrime: Double,
  predictions: Seq[CriticalPowerPrediction]
)

object CriticalPowerPrediction {
  implicit val writes = Json.writes[CriticalPowerPrediction]
}

object CriticalPower {
  implicit val writes = Json.writes[CriticalPower]
}
