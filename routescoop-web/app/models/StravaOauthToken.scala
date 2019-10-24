package models

import services.OauthTokenResponse

import play.api.libs.json.Json

import java.time.Instant

case class StravaOauthToken(accessToken: String, expiresAt: Instant, refreshToken: String, athleteId: Int) {
  def isExpired = expiresAt.isBefore(Instant.now)
}

object StravaOauthToken {
  implicit val stravaTokenFormat = Json.format[StravaOauthToken]

  def apply(tokenResponse: OauthTokenResponse): StravaOauthToken = {
    StravaOauthToken(
      tokenResponse.access_token,
      Instant.ofEpochSecond(tokenResponse.expires_at),
      tokenResponse.refresh_token,
      tokenResponse.athlete_id
    )
  }
}
