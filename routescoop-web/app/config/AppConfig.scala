package config

import javax.inject.{Inject, Provider}
import play.api.Configuration

case class AppConfig(apiHost: String, apiPath: String)

object AppConfig {

  def apply(configuration: Configuration): AppConfig = {
    new AppConfig(
      configuration.getString("routescoop.apiHost").get,
      configuration.getString("routescoop.apiPath").get
    )
  }

}

class AppConfigProvider @Inject()(configuration: Configuration) extends Provider[AppConfig] {
  override def get(): AppConfig = AppConfig(configuration)
}