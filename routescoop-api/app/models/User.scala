package models

import java.time.Instant
import java.util.UUID
import anorm.{Macro, RowParser}

import play.api.libs.json.Json

import java.time.temporal.ChronoUnit


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
  maxHeartRate: Int,
  createdAt: Instant = Instant.now.truncatedTo(ChronoUnit.SECONDS)
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
  createdAt: Instant = Instant.now.truncatedTo(ChronoUnit.SECONDS) // https://stackoverflow.com/questions/47198806/how-to-store-a-java-instant-in-a-mysql-database
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
      create.maxHeartRate,
      create.createdAt.truncatedTo(ChronoUnit.SECONDS)
    )

}
