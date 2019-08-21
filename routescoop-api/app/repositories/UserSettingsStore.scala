package repositories

import javax.inject.{Inject, Singleton}
import anorm._

import com.typesafe.scalalogging.LazyLogging
import models.UserSettings
import modules.BlockingContext

import play.api.db.Database

import java.time.Instant
import scala.concurrent.ExecutionContext

trait UserSettingsStore {

  val UserSettingsTable = "user_settings"

  def insert(settings: UserSettings): Unit
  def destroy(): Unit
  def findById(id: String): Option[UserSettings]
  def findByUserId(userId: String): Seq[UserSettings]
  def findLatestUntil(date: Instant, userId: String): Option[UserSettings]
  def findEarliestAfter(date: Instant, userId: String): Option[UserSettings]
  def delete(id: String): Unit

}

@Singleton
class UserSettingsSqlStore @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends UserSettingsStore with LazyLogging {

  override def insert(settings: UserSettings): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$UserSettingsTable (id, userId, weight, ftp, maxHeartRate, createdAt)
          VALUES (
            ${settings.id},
            ${settings.userId},
            ${settings.weight},
            ${settings.ftp},
            ${settings.maxHeartRate},
            ${settings.createdAt}
          )
      """.executeInsert()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$UserSettingsTable
      """.execute()
  }

  override def findById(id: String): Option[UserSettings] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * from #$UserSettingsTable WHERE id = $id
      """.as(UserSettings.parser.singleOpt)
  }

  override def delete(id: String): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$UserSettingsTable WHERE id = $id
      """.execute()
  }

  override def findByUserId(userId: String): Seq[UserSettings] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$UserSettingsTable WHERE userId = $userId ORDER BY createdAt DESC
      """.as(UserSettings.parser.*)
  }

  override def findLatestUntil(date: Instant, userId: String) = db.withConnection { implicit conn =>
    SQL"""
          SELECT *
          FROM #$UserSettingsTable us1
          WHERE us1.createdAt = ( SELECT max(createdAt)
                                  FROM user_settings us2
                                  WHERE us2.userId = us1.userId
                                    AND us2.createdAt <= $date)
            AND us1.userId = $userId
      """.as(UserSettings.parser.singleOpt)
  }

  override def findEarliestAfter(date: Instant, userId: String) =
    db.withConnection { implicit conn =>
      SQL"""
            SELECT us.*
            FROM #$UserSettingsTable us
            WHERE us.createdAt > $date
              AND us.userId = $userId
            ORDER BY us.createdAt
            LIMIT 1
        """.as(UserSettings.parser.singleOpt)
    }

}
