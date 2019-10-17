package models

import play.api.libs.json.Json

case class User(
  id: String,
  name: String,
  email: String,
  picture: String,
  stravaToken: Option[String] = None,
  stravaId: Option[Int] = None,
  stravaOauthToken: Option[StravaOauthToken] = None
) {
  def toProfile = Profile(id, name, email, picture, stravaToken, stravaId, stravaOauthToken)
}

object User {
  implicit val format = Json.format[User]

}

sealed trait UserResult
case class UserResultSuccess(user: User) extends UserResult
case class UserResultError(message: String) extends UserResult
case object UserResultNotFound extends UserResult


