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
      configuration.get[String]("strava.clientId"),
      configuration.get[String]("strava.clientSecret"),
      configuration.get[String]("strava.authorizationUrl"),
      configuration.get[String]("strava.redirectUrl"),
      configuration.get[String]("strava.oauthUrl")
    )
  }
}

class StravaConfigProvider @Inject()(configuration: Configuration) extends Provider[StravaConfig] {
  override def get(): StravaConfig = StravaConfig(configuration)
}
