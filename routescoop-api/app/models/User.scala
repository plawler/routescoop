package models

import anorm.{Macro, RowParser}
import play.api.libs.json.Json

/**
  * Created by paullawler on 12/27/16.
  */
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