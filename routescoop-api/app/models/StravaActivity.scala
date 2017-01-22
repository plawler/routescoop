package models

import anorm.{Macro, RowParser}
import kiambogo.scrava.models.PersonalActivitySummary

import com.fasterxml.uuid.Generators

import java.time.Instant
import java.util.UUID

sealed trait Activity

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
  workoutType: Option[Int] = None
) extends Activity

object StravaActivity {

  implicit val parser: RowParser[StravaActivity] = Macro.namedParser[StravaActivity]

}

case class StravaActivityCreated(activity: StravaActivity, createdAt: Instant = Instant.now)