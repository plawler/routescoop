package models


import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Profile(
  userId: String,
  name: String,
  email: String,
  picture: String,
  stravaToken: Option[String],
  stravaId: Option[Int]
)

object Profile {

  implicit val profileReads: Reads[Profile] = (
    (JsPath \ "user_id").read[String].map(_.split('|')(1)) and
      (JsPath \ "name").read[String].map(_.split("@")(0)) and
      (JsPath \ "email").read[String] and
      (JsPath \ "picture").read[String] and
      (JsPath \ "stravaToken").readNullable[String] and
      (JsPath \ "stravaId").readNullable[Int]
    ) (Profile.apply _)

  implicit val profileWrites: Writes[Profile] = Json.writes[Profile]

}