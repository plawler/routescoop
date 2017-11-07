package models

import anorm.{Macro, RowParser}
import kiambogo.scrava.models.PersonalActivitySummary

import java.time.Instant
import java.util.UUID

sealed trait Activity {
  val id: String
  val startedAt: Instant
}

/**
  * distances are in meters
  * time values in seconds
  */
case class StravaActivity(
  id: String,
  userId: String,
  stravaId: Int,
  athleteId: Int,
  name: String,
  distance: Double,
  movingTime: Int,
  elapsedTime: Int,
  totalElevationGain: Double,
  activityType: String,
  startedAt: Instant,
  timezone: String,
  startLat: Double,
  startLong: Double,
  trainer: Boolean,
  commute: Boolean,
  manual: Boolean,
  averageSpeed: Double,
  maxSpeed: Double,
  externalId: Option[String] = None,
  endLat: Option[Double] = None,
  endLong: Option[Double] = None,
  mapPolyLine: Option[String] = None,
  mapPolyLineSummary: Option[String] = None,
  averageCadence: Option[Double] = None,
  averageTemp: Option[Int] = None,
  averageWatts: Option[Double] = None,
  weightedAverageWatts: Option[Int] = None,
  kilojoules: Option[Double] = None,
  deviceWatts: Option[Boolean] = None,
  averageHeartRate: Option[Double] = None,
  maxHeartRate: Option[Double] = None,
  workoutType: Option[Int] = None,
  dataSyncId: Option[String] = None
) extends Activity

object StravaActivity {

  implicit val parser: RowParser[StravaActivity] = Macro.namedParser[StravaActivity]

  def create(user: User, summary: PersonalActivitySummary): StravaActivity = {
    StravaActivity(
      UUID.randomUUID().toString,
      user.id,
      stravaId = summary.id,
      user.stravaId.get, // todo: handle this
      summary.name,
      summary.distance,
      summary.moving_time,
      summary.elapsed_time,
      summary.total_elevation_gain,
      summary.`type`,
      Instant.parse(summary.start_date),
      summary.timezone,
      summary.start_latlng.head,
      summary.start_latlng(1),
      summary.trainer,
      summary.commute,
      summary.manual,
      summary.average_speed,
      summary.max_speed,
      summary.external_id,
      summary.end_latlng.map(_.head.toDouble),
      summary.end_latlng.map(_ (1).toDouble),
      summary.map.summary_polyline,
      summary.map.polyline,
      summary.average_cadence.map(_.toDouble),
      summary.average_temp,
      summary.average_watts.map(_.toDouble),
      weightedAverageWatts = None,
      summary.kilojoules.map(_.toDouble),
      summary.device_watts,
      summary.average_heartrate.map(_.toDouble),
      summary.max_heartrate.map(_.toDouble),
      summary.workout_type
    )
  }

}

case class StravaActivityCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaLapsCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaStreamsCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaActivitySyncCompleted(activity: StravaActivity, createdAt: Instant = Instant.now)

