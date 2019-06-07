package models

import play.api.libs.json._

sealed trait SimulationType { def name: String }

case object CP extends SimulationType { val name = "CriticalPower" }

object SimulationType {
  implicit object SimulationModelFormat extends Format[SimulationType] {
    override def reads(json: JsValue) = {
      json match {
        case JsString("CriticalPower") => JsSuccess(CP)
        case _ => JsError("cannot parse simulation model")
      }
    }
    override def writes(st: SimulationType) = JsString(st.name)
  }
}

case class Simulation(simulationType: SimulationType, parameters: Map[String,String])

object Simulation {
  implicit val format = Json.format[Simulation]
}

case class SimulationResult(simulationType: SimulationType, result: PowerModel)

object SimulationResult {
  implicit val writes = Json.writes[SimulationResult]
}
