package models

import java.time.{Instant, LocalDate}
import java.util.Date

import anorm.{Macro, RowParser}
import play.api.libs.json.Json

sealed trait Stats

case class ActivityStats(
  activityId: String,
  userSettingsId: String, // settings used at time of activity
  averagePower: Int,
  normalizedPower: Int,
  stressScore: Int,
  intensityFactor: Double,
  variabilityIndex: Double
//  ,
//  cogganZoneOne: Int,
//  seilerZoneOne: Int
) extends Stats

object ActivityStats {

  implicit val activityStatsFormat = Json.format[ActivityStats]
  implicit val parser: RowParser[ActivityStats] = Macro.namedParser[ActivityStats]

}

case class DailyStress(day: LocalDate, stressScore: Int)

object DailyStress {
  implicit val format = Json.format[ActivityStats]
  implicit val parser = Macro.namedParser[DailyStress]
}

case class DailyTrainingLoad(
  date: LocalDate,
  fitness: Double,
  fatigue: Double,
  rampRate: Double,
  stressBalance: Double
)

object DailyTrainingLoad {
  implicit val format = Json.format[DailyTrainingLoad]
}

