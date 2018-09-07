package config

import javax.inject.{Inject, Provider}

import play.api.Configuration

case class StravaConfig(
  clientId: String,
  clientSecret: String,
  authorizationUrl: String,
  redirectUri: String,
  oauthUrl: String
)

object StravaConfig {
  def apply(configuration: Configuration): StravaConfig = {
    StravaConfig(
      configuration.getString("strava.clientId").get,
      configuration.getString("strava.clientSecret").get,
      configuration.getString("strava.authorizationUrl").get,
      configuration.getString("strava.redirectUrl").get,
      configuration.getString("strava.oauthUrl").get
    )
  }
}

class StravaConfigProvider @Inject()(configuration: Configuration) extends Provider[StravaConfig] {
  override def get(): StravaConfig = StravaConfig(configuration)
}