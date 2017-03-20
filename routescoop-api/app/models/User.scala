package models

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