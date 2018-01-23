package repositories

import javax.inject.{Inject, Singleton}

import anorm._
import com.typesafe.scalalogging.LazyLogging
import models.UserSettings
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext

trait UserSettingsStore {

  val UserSettingsTable = "user_settings"

  def insert(settings: UserSettings): Unit
  def destroy(): Unit
  def findById(id: String): Option[UserSettings]
  def findByUserId(userId: String): Seq[UserSettings]
  def delete(id: String): Unit

}

@Singleton
class UserSettingsSqlStore @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends UserSettingsStore with LazyLogging {

  override def insert(settings: UserSettings): Unit = db.withTransaction { implicit conn =>
    logger.info(s"Inserting the settings: $settings")
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
          SELECT * FROM #$UserSettingsTable WHERE userId = $userId ORDER BY createdAt
      """.as(UserSettings.parser.*)
  }
}
