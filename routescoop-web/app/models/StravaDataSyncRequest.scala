package models

import play.api.libs.json.Json


case class StravaDataSyncRequest(token: String, athleteId: Int)

object StravaDataSyncRequest {

  implicit val format = Json.format[StravaDataSyncRequest]

}
