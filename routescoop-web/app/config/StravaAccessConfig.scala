package config

import javax.inject.{Inject, Provider}

import play.api.Configuration

case class StravaAccessConfig(
  clientId: String,
  clientSecret: String,
  authorizationUrl: String,
  redirectUri: String,
  oauthUrl: String
) {
  def forTokenExchange(code: String): Map[String, Seq[String]] =
   Map("client_id" -> Seq(clientId), "client_secret" -> Seq(clientSecret), "code" -> Seq(code))
}

object StravaAccessConfig {
  def apply(configuration: Configuration): StravaAccessConfig = {
    StravaAccessConfig(
      configuration.getString("strava.clientId").get,
      configuration.getString("strava.clientSecret").get,
      configuration.getString("strava.authorizationUrl").get,
      configuration.getString("strava.redirectUrl").get,
      configuration.getString("strava.oauthUrl").get
    )
  }
}

class StravaAccessConfigProvider @Inject()(configuration: Configuration) extends Provider[StravaAccessConfig] {
  override def get(): StravaAccessConfig = StravaAccessConfig(configuration)
}