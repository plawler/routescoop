package config

import javax.inject.{Provider, Inject}

import play.api.Configuration


case class AuthConfig(
  clientSecret: String,
  clientId: String,
  domain: String,
  callbackUrl: String,
  tokenUrl: String,
  fetchUserUrl: String,
  logoutUrl: String,
  audience: Option[String] = None
) {
  val getAudience = audience.getOrElse(fetchUserUrl)
}

object AuthConfig {
  def apply(configuration: Configuration): AuthConfig = {
    AuthConfig(
      configuration.get[String]("auth.clientSecret"),
      configuration.get[String]("auth.clientId"),
      configuration.get[String]("auth.domain"),
      configuration.get[String]("auth.callbackUrl"),
      configuration.get[String]("auth.tokenUrl"),
      configuration.get[String]("auth.fetchUserUrl"),
      configuration.get[String]("auth.logoutUrl"),
      configuration.getOptional[String]("auth.audience")
    )
  }
}

class AuthConfigProvider @Inject()(configuration: Configuration) extends Provider[AuthConfig] {

  // basically provides a factory style approach to wiring up the dependency
  override def get(): AuthConfig = {
    AuthConfig(configuration)
  }

}

