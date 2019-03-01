package models

import java.time.{Instant, LocalDate}
import play.api.libs.json.Json

case class Settings(id: String, weight: Int, ftp: Int, maxHeartRate: Int, createdAt: Instant)
object Settings {
  implicit val format = Json.format[Settings]
}

case class NewSettings(userId: String, weight: Int, ftp: Int, maxHeartRate: Int, createdOn: LocalDate = LocalDate.now())
object NewSettings {
  implicit val format = Json.format[NewSettings]
}

sealed trait SettingsResult
case class SettingsResultSuccess(settings: Seq[Settings]) extends SettingsResult
case class SettingsResultError(message: String) extends SettingsResult
