package models

import java.time.Instant
import java.util.UUID
import anorm.{Macro, RowParser}
import services.LapEffort

sealed trait Lap

case class StravaLap(
  id: String,
  activityId: String,
  stravaId: Long,
  stravaActivityId: Long,
  athleteId: Long,
  resourceState: Int,
  name: String,
  elapsedTime: Int,
  movingTime: Int,
  startedAt: Instant,
  distance: Double,
  startIndex: Int,
  endIndex: Int,
  lapIndex: Int,
  totalElevationGain: Double,
  averageSpeed: Double,
  maxSpeed: Double,
  averageWatts: Double,
  deviceWatts: Option[Boolean] = None,
  averageCadence: Option[Double] = None,
  averageHeartRate: Option[Double] = None,
  maxHeartRate: Option[Double] = None
) extends Lap

object StravaLap {

  implicit val parser: RowParser[StravaLap] = Macro.namedParser[StravaLap]

  def create(activity: StravaActivity, lap: LapEffort): StravaLap = {
    StravaLap(
      UUID.randomUUID().toString,
      activity.id,
      lap.id,
      activity.stravaId,
      activity.athleteId,
      lap.resource_state,
      lap.name,
      lap.elapsed_time,
      lap.moving_time,
      Instant.parse(lap.start_date),
      lap.distance,
      lap.start_index,
      lap.end_index,
      lap.lap_index,
      lap.total_elevation_gain,
      lap.average_speed,
      lap.max_speed,
      lap.average_watts,
      lap.device_watts,
      lap.average_cadence.map(_.toDouble),
      lap.average_heartrate.map(_.toDouble),
      lap.max_heartrate.map(_.toDouble)
    )
  }

  def fromScathleteLap(activity: StravaActivity, lap: scathlete.models.StravaLap) = {
    StravaLap(
      UUID.randomUUID().toString,
      activity.id,
      lap.id,
      activity.stravaId,
      activity.athleteId,
      lap.resource_state,
      lap.name,
      lap.elapsed_time,
      lap.moving_time,
      Instant.parse(lap.start_date),
      lap.distance,
      lap.start_index,
      lap.end_index,
      lap.lap_index,
      lap.total_elevation_gain,
      lap.average_speed,
      lap.max_speed,
      lap.average_watts,
      lap.device_watts,
      lap.average_cadence.map(_.toDouble),
      lap.average_heartrate.map(_.toDouble),
      lap.max_heartrate.map(_.toDouble)
    )
  }

}
