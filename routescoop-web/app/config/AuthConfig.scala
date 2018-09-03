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
      configuration.getString("auth.clientSecret").get,
      configuration.getString("auth.clientId").get,
      configuration.getString("auth.domain").get,
      configuration.getString("auth.callbackUrl").get,
      configuration.getString("auth.tokenUrl").get,
      configuration.getString("auth.fetchUserUrl").get,
      configuration.getString("auth.logoutUrl").get,
      configuration.getString("auth.audience")
    )
  }
}

class AuthConfigProvider @Inject()(configuration: Configuration) extends Provider[AuthConfig] {

  // basically provides a factory style approach to wiring up the dependency
  override def get(): AuthConfig = {
    AuthConfig(configuration)
  }

}

