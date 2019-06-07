package models

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait PowerModel

case class CriticalPowerPrediction(
  duration: Int,
  watts: Int
)

object CriticalPowerPrediction {
  implicit val format = Json.format[CriticalPowerPrediction]
}

case class CriticalPower(
  cp: Double,
  wPrime: Double,
  predictions: Seq[CriticalPowerPrediction]
) extends PowerModel

// circular dependency with
object PowerModel {
  implicit object PowerModelWrites extends Writes[PowerModel] {
    override def writes(pm: PowerModel): JsValue = {
      pm match {
        case cp: CriticalPower => Json.obj("cp" -> cp.cp, "wPrime" -> cp.wPrime, "predictions" -> cp.predictions)
      }
    }
  }
}

