package models

import anorm.{Macro, RowParser}

import play.api.libs.json.Json

import java.time.Instant

case class StravaOauthToken(userId: String, accessToken: String, expiresAt: Instant, refreshToken: String)

object StravaOauthToken {
  implicit val format = Json.format[StravaOauthToken]
  implicit val parser: RowParser[StravaOauthToken] = Macro.namedParser[StravaOauthToken]
}
