package repositories

import java.time.Instant

import anorm.{Macro, RowParser}


case class StoredDataSyncRequest(id: String, userId: String, startedAt: Instant, completedAt: Option[Instant])

object StoredDataSyncRequest {

  implicit val parser: RowParser[StoredDataSyncRequest] = Macro.namedParser[StoredDataSyncRequest]

}