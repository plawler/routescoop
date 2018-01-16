package models

import java.time.Instant
import java.util.UUID

import anorm.{Macro, RowParser}
import play.api.libs.json.Json


case class User(
  id: String,
  name: String,
  email: String,
  picture: Option[String] = None,
  stravaToken: Option[String] = None,
  stravaId: Option[Int] = None
)

object User {

  implicit val userFormat = Json.format[User]

  implicit val parser: RowParser[User] = Macro.namedParser[User]

}

case class CreateUserSettings(
  userId: String,
  weight: Int,
  ftp: Int,
  maxHeartRate: Int
)

object CreateUserSettings {
  implicit val createSettingsFormat = Json.format[CreateUserSettings]
}

case class UserSettings(
  id: String,
  userId: String,
  weight: Int,
  ftp: Int,
  maxHeartRate: Int,
  createdAt: Instant = Instant.now
)

object UserSettings {

  implicit val settingsFormat = Json.format[UserSettings]

  implicit val parser: RowParser[UserSettings] = Macro.namedParser[UserSettings]

  def of(create: CreateUserSettings): UserSettings =
    UserSettings(
      UUID.randomUUID().toString,
      create.userId,
      create.weight,
      create.ftp,
      create.maxHeartRate
    )
}