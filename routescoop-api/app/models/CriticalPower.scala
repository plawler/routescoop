package models

import play.api.libs.json.Json

case class CriticalPower(cp: Double, wPrime: Double, predictedPower: Seq[Double])

object CriticalPower {
  implicit val writes = Json.writes[CriticalPower]
}
