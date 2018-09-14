package models

import java.time.Instant

import play.api.libs.json.Json

case class NewSettings(userId: String, weight: Int, ftp: Int, maxHeartRate: Int)
object NewSettings {
  implicit val format = Json.format[NewSettings]
}

case class Settings(id: String, weight: Int, ftp: Int, maxHeartRate: Int, createdAt: Instant)
object Settings {
  implicit val format = Json.format[Settings]
}

sealed trait SettingsResult
case class SettingsResultSuccess(settings: Seq[Settings]) extends SettingsResult
case class SettingsResultError(message: String) extends SettingsResult