package repositories

import java.time.Instant

import anorm.{Macro, RowParser}


case class StoredUserDataSync(id: String, userId: String, startedAt: Instant, completedAt: Option[Instant] = None)

object StoredUserDataSync {

  implicit val parser: RowParser[StoredUserDataSync] = Macro.namedParser[StoredUserDataSync]

}
