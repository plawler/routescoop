package models

import anorm.{Macro, RowParser}

/**
  * distances are in meters
  * time values in seconds
  */
case class StravaActivity(
  id: String,
  athleteId: String,
  name: String,
  distance: Float,
  movingTime: Int,
  elapsedTime: Int,
  totalElevationGain: Float,
  activityType: String,
  startDate: String,
  startDateLocal: String,
  timezone: String,
  startLat: Float,
  startLong: Float,
  trainer: Boolean,
  commute: Boolean,
  manual: Boolean,
  averageSpeed: Float,
  maxSpeed: Float,
  externalId: Option[String] = None, // file reference
  endLat: Option[Float] = None,
  endLong: Option[Float] = None,
  averageCadence: Option[Float] = None,
  averageTemp: Option[Int] = None,
  averageWatts: Option[Float] = None,
  weightedAverageWatts: Option[Int] = None,
  kilojoules: Option[Float] = None,
  deviceWatts: Option[Boolean] = None,
  averageHeartrate: Option[Float] = None,
  maxHeartrate: Option[Float] = None,
  workoutType: Option[Int] = None
)

object StravaActivity {

  implicit val parser: RowParser[StravaActivity] = Macro.namedParser[StravaActivity]

}
