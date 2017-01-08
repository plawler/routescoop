package models

import java.time.Instant

import play.api.libs.json.Json


case class DataSyncRequest(user: User, startedAt: Instant = Instant.now, completedAt: Option[Instant] = None)

object DataSyncRequest {

  implicit val syncRequestFormat = Json.format[DataSyncRequest]

}
