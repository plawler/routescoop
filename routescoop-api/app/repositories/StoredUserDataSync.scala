package repositories

import java.time.Instant
import java.util.UUID

import anorm.{Macro, RowParser}


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

}
