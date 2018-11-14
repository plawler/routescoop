package modules

import javax.inject.{Inject, Provider}
import play.api.Configuration

case class AppConfig(pageSize: Int)

object AppConfig {

  def apply(configuration: Configuration): AppConfig = {
    AppConfig(
      configuration.getInt("api.pageSize").get
    )
  }

}

class AppConfigProvider @Inject()(configuration: Configuration) extends Provider[AppConfig] {
  override def get() = AppConfig(configuration)
}
