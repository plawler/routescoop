package models

import java.time.Instant
import play.api.libs.json.Json

case class Ride(
  id: String,
  userId: String,
  stravaId: Long,
  athleteId: Int,
  name: String,
  startedAt: Instant,
  distance: Double,
  movingTime: Int,
  elapsedTime: Int,
  totalElevationGain: Double,
  activityType: String,
  trainer: Boolean,
  commute: Boolean,
  manual: Boolean,
  averageSpeed: Double,
  maxSpeed: Double,
  externalId: Option[String] = None,
  location: RideLocation,
  powerHr: RidePowerHr,
  analysis: Option[RideAnalysis] = None
)

case class RideLocation(
  timezone: String,
  startLat: Double,
  startLong: Double,
  endLat: Option[Double] = None,
  endLong: Option[Double] = None,
  mapPolyLine: Option[String] = None,
  mapPolyLineSummary: Option[String] = None
)

case class RidePowerHr(
  averageCadence: Option[Double] = None,
  averageWatts: Option[Double] = None,
  weightedAverageWatts: Option[Int] = None,
  kilojoules: Option[Double] = None,
  deviceWatts: Option[Boolean] = None,
  averageHeartRate: Option[Double] = None,
  maxHeartRate: Option[Double] = None,
  workoutType: Option[Int] = None
)

case class RideAnalysis(
  activityId: String,
  userSettingsId: String, // settings used at time of activity
  averagePower: Int,
  normalizedPower: Int,
  stressScore: Int,
  intensityFactor: Double,
  variabilityIndex: Double
)

object RideAnalysis {
  implicit val analysisFormat = Json.format[RideAnalysis]
}

object Ride {
  implicit val locationReads = Json.reads[RideLocation]
  implicit val powerHrReads = Json.reads[RidePowerHr]
  implicit val rideReads = Json.reads[Ride]
}

case class RideSync(id: String, userId: String, startedAt: Instant, completedAt: Option[Instant] = None)
object RideSync { implicit val format = Json.format[RideSync] }

case class RideSummary(
  id: String,
  name: String,
  startedAt: Instant,
  distance: Double,
  movingTime: Int,
  analysisCompleted: Boolean = false
)
object RideSummary { implicit val format = Json.format[RideSummary] }

sealed trait RideSyncResult
case class RideSyncResultStarted(sync: RideSync) extends RideSyncResult
case class RideSyncResultCompleted(sync: RideSync) extends RideSyncResult
case class RideSyncResultError(message: String) extends RideSyncResult

sealed trait RideSummaryResult
case class RideSummaryResultSuccess(summaries: Seq[RideSummary]) extends RideSummaryResult
case class RideSummaryResultError(message: String) extends RideSummaryResult

sealed trait RideDetailsResult
case class RideDetailsResultSuccess(ride: Ride) extends RideDetailsResult
case class RideDetailsResultError(message: String) extends RideDetailsResult

