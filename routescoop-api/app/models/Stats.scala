package models

import anorm.{Macro, RowParser}

import play.api.libs.json.Json

import java.time.LocalDate

sealed trait Stats

case class ActivityStats(
  activityId: String,
  userSettingsId: String, // settings used at time of activity
  averagePower: Int,
  normalizedPower: Int,
  stressScore: Int,
  intensityFactor: Double,
  variabilityIndex: Double
) extends Stats

object ActivityStats {
  implicit val activityStatsFormat = Json.format[ActivityStats]
  implicit val parser: RowParser[ActivityStats] = Macro.namedParser[ActivityStats]
}

case class DailyStress(day: LocalDate, stressScore: Int, week: Int = -1)

object DailyStress {
  implicit val format = Json.format[ActivityStats]
  implicit val parser = Macro.namedParser[DailyStress]
}

case class DailyTrainingLoad(
  date: LocalDate,
  fitness: Double,
  fatigue: Double,
  stressBalance: Double
)

object DailyTrainingLoad {
  implicit val format = Json.format[DailyTrainingLoad]
}

case class RampRate(sixWeekAvgs: Seq[Int], ramps: Seq[Int])

object RampRate {
  implicit val format = Json.format[RampRate]
}

case class FitnessTrend(trainingLoad: Seq[DailyTrainingLoad], rampRate: RampRate)

object FitnessTrend {
  implicit val format = Json.format[FitnessTrend]
}

