package models

import java.time.Instant
import java.util.{Date, UUID}
import anorm.{Macro, RowParser}
import scathlete.models.StravaSummaryActivity

import play.api.libs.json.Json
import services.SummaryActivity

sealed trait Activity {
  val id: String
  val userId: String
  val startedAt: Instant
}

/**
  * distances are in meters
  * time values in seconds
  */
case class StravaActivity(
  id: String,
  userId: String,
  stravaId: Long,
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

  def create(user: User, sa: SummaryActivity): StravaActivity = {
    StravaActivity(
      UUID.randomUUID().toString,
      user.id,
      sa.info.id,
      user.stravaId.get,
      sa.info.name,
      sa.info.distance,
      sa.info.moving_time,
      sa.info.elapsed_time,
      sa.info.total_elevation_gain,
      sa.info.`type`,
      sa.info.start_date,
      sa.info.timezone,
      sa.location.start_latitude.getOrElse(0.0),
      sa.location.start_longitude.getOrElse(0.0),
      sa.info.trainer,
      sa.info.commute,
      sa.info.manual,
      sa.performance.average_speed,
      sa.performance.max_speed,
      Some(sa.info.external_id),
      endLat = None,
      endLong = None,
      mapPolyLine = None,
      sa.map.summary_polyline,
      sa.performance.average_cadence,
      averageTemp = None,
      sa.performance.average_watts,
      sa.performance.weighted_average_watts,
      sa.performance.kilojoules,
      sa.performance.device_watts,
      sa.performance.average_heartrate,
      sa.performance.max_heartrate
    )
  }

  def create(userId: String, summary: StravaSummaryActivity): StravaActivity = {
    StravaActivity(
      UUID.randomUUID().toString,
      userId,
      summary.info.id,
      summary.athlete.id.toInt,
      summary.info.name,
      summary.info.distance,
      summary.info.moving_time,
      summary.info.elapsed_time,
      summary.info.total_elevation_gain,
      summary.info.`type`,
      summary.info.start_date,
      summary.info.timezone,
      summary.location.start_latitude.getOrElse(0.0),
      summary.location.start_longitude.getOrElse(0.0),
      summary.info.trainer,
      summary.info.commute,
      summary.info.manual,
      summary.performance.average_speed,
      summary.performance.max_speed,
      summary.info.external_id,
      endLat = None,
      endLong = None,
      mapPolyLine = None,
      summary.map.summary_polyline,
      summary.performance.average_cadence,
      averageTemp = None,
      summary.performance.average_watts,
      summary.performance.weighted_average_watts,
      summary.performance.kilojoules,
      summary.performance.device_watts,
      summary.performance.average_heartrate,
      summary.performance.max_heartrate
    )
  }
}

case class Summary(
  id: String,
  name: String,
  startedAt: Instant,
  distance: Double,
  movingTime: Int,
  analysisCompleted: Boolean = false
)

object Summary {

  import utils.AnormExtension.rowToBoolean

  implicit val format = Json.format[Summary]
  implicit val parser = Macro.namedParser[Summary]

}

case class StravaActivityCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaLapsCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaStreamsCreated(activity: StravaActivity, createdAt: Instant = Instant.now)
case class StravaActivitySyncCompleted(activity: StravaActivity, createdAt: Instant = Instant.now)

