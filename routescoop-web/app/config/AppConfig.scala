package config

import javax.inject.{Inject, Provider}
import play.api.Configuration

case class AppConfig(apiHost: String, apiPath: String) {
  val baseApiUrl = s"$apiHost$apiPath"
  val pingUrl = s"$apiHost/ping"
}

object AppConfig {

  def apply(configuration: Configuration): AppConfig = {
    new AppConfig(
      configuration.get[String]("routescoop.apiHost"),
      configuration.get[String]("routescoop.apiPath")
    )
  }

}

class AppConfigProvider @Inject()(configuration: Configuration) extends Provider[AppConfig] {
  override def get(): AppConfig = AppConfig(configuration)
}
