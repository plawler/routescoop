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

}
