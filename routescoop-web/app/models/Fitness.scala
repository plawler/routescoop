package models

import play.api.libs.json.Json

import java.time.LocalDate

case class DailyFitness(
  date: LocalDate,
  fitness: Double,
  fatigue: Double,
  stressBalance: Double
)

object DailyFitness {
  implicit val format = Json.format[DailyFitness]
}

sealed trait FitnessTrendResult
case class FitnessTrendResultSuccess(trend: Seq[DailyFitness]) extends FitnessTrendResult
case class FitnessTrendResultError(message: String) extends FitnessTrendResult
