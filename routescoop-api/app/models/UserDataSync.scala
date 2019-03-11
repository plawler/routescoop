package models

import java.time.Instant

import play.api.libs.json.{Json, OFormat}

case class UserDataSyncRequest(userId: String, fetchOlderRides: Boolean)
object UserDataSyncRequest {
  implicit val reads = Json.reads[UserDataSyncRequest]
}

case class UserDataSync(id: String, userId: String, startedAt: Instant, previous: Boolean = false)
object UserDataSync {
  implicit val dataSyncFormat: OFormat[UserDataSync] = Json.format[UserDataSync]
}

case class StravaDataSyncStarted(sync: UserDataSync)
case class StravaDataSyncCompleted(syncId: String, completedAt: Instant = Instant.now)



