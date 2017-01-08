package repositories

import java.sql.Connection
import java.time.Instant
import javax.inject.{Inject, Singleton}

import anorm._
import com.fasterxml.uuid.Generators
import play.api.db.Database


trait DataSyncRequestStore {

  val DataSyncRequestsTable = "data_sync_requests"

  def insert(userId: String, startedAt: Instant): StoredDataSyncRequest
  def update(id: String, completedAt: Instant): Int
  def findById(id: String): Option[StoredDataSyncRequest]
  def findByUserId(userId: String): Seq[StoredDataSyncRequest]
  def destroy(): Unit

}

@Singleton
class DataSyncRequestSqlStore @Inject()(db: Database) extends DataSyncRequestStore {

  override def insert(userId: String, startedAt: Instant): StoredDataSyncRequest = db.withTransaction { implicit conn =>
    val id = Generators.randomBasedGenerator().generate().toString
      SQL"""
          INSERT INTO #$DataSyncRequestsTable (id, userId, startedAt)
          VALUES ($id, $userId, $startedAt)
      """.executeInsert()
    select(id).getOrElse(throw new IllegalStateException(s"Failed to retrieve sync request $id after insert"))
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$DataSyncRequestsTable
      """.execute()
  }

  override def update(id: String, completedAt: Instant): Int = db.withTransaction { implicit conn =>
    SQL"""
          UPDATE #$DataSyncRequestsTable SET completedAt = $completedAt WHERE id = $id
      """.executeUpdate()
  }

  private def select(id: String)(implicit conn: Connection): Option[StoredDataSyncRequest] = {
    SQL"""
          SELECT *
          FROM #$DataSyncRequestsTable
          WHERE id = $id
    """.as(StoredDataSyncRequest.parser.singleOpt)
  }

  override def findById(id: String): Option[StoredDataSyncRequest] = db.withConnection { implicit conn =>
    select(id)
  }

  override def findByUserId(userId: String): Seq[StoredDataSyncRequest] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$DataSyncRequestsTable WHERE userId = $userId
      """.as(StoredDataSyncRequest.parser.*)
  }

}
