package repositories

import java.sql.Connection
import java.time.Instant
import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.Database


trait UserDataSyncStore {

  val UserDataSyncsTable = "user_data_syncs"

  def insert(sync: StoredUserDataSync): Unit
  def update(id: String, completedAt: Instant): Int
  def findById(id: String): Option[StoredUserDataSync]
  def findByUserId(userId: String): Seq[StoredUserDataSync]
  def destroy(): Unit

}

@Singleton
class UserDataSyncSqlStore @Inject()(db: Database) extends UserDataSyncStore {

  override def insert(sync: StoredUserDataSync): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$UserDataSyncsTable (id, userId, startedAt)
          VALUES (${sync.id}, ${sync.userId}, ${sync.startedAt})
      """.executeInsert()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$UserDataSyncsTable
      """.execute()
  }

  override def update(id: String, completedAt: Instant): Int = db.withTransaction { implicit conn =>
    SQL"""
          UPDATE #$UserDataSyncsTable SET completedAt = $completedAt WHERE id = $id
      """.executeUpdate()
  }

  private def select(id: String)(implicit conn: Connection): Option[StoredUserDataSync] = {
    SQL"""
          SELECT *
          FROM #$UserDataSyncsTable
          WHERE id = $id
    """.as(StoredUserDataSync.parser.singleOpt)
  }

  override def findById(id: String): Option[StoredUserDataSync] = db.withConnection { implicit conn =>
    select(id)
  }

  override def findByUserId(userId: String): Seq[StoredUserDataSync] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$UserDataSyncsTable WHERE userId = $userId
      """.as(StoredUserDataSync.parser.*)
  }

}
