package repositories

import java.time.Instant
import java.util.UUID
import anorm.{Macro, RowParser}
import models.UserDataSyncRequest


case class StoredUserDataSync(id: String, userId: String, startedAt: Instant, completedAt: Option[Instant] = None)

object StoredUserDataSync {

  implicit val parser: RowParser[StoredUserDataSync] = Macro.namedParser[StoredUserDataSync]

  def create(userId: String) = {
    StoredUserDataSync(
      UUID.randomUUID().toString,
      userId = userId,
      startedAt = Instant.now
    )
  }

  def of(dsr: UserDataSyncRequest) = {
    StoredUserDataSync(
      UUID.randomUUID().toString,
      userId = dsr.userId,
      startedAt = Instant.now
    )
  }

}
