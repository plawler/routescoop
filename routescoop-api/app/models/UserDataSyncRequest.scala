package models

import java.time.Instant

import play.api.libs.json.Json


case class UserDataSyncRequest(user: User)

case class StravaDataSyncStarted(id: String, userId: String)

object UserDataSyncRequest {

  implicit val syncRequestFormat = Json.format[UserDataSyncRequest]

}
