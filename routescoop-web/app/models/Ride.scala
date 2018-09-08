package models

import java.time.Instant

import play.api.libs.json.Json

case class Ride()

case class RideSync(id: String, userId: String, startedAt: Instant, completedAt: Option[Instant] = None)
object RideSync { implicit val format = Json.format[RideSync] }

sealed trait RideSyncResult
case class RideSyncResultStarted(sync: RideSync) extends RideSyncResult
case class RideSyncResultCompleted(sync: RideSync) extends RideSyncResult
case class RideSyncResultError(message: String) extends RideSyncResult
