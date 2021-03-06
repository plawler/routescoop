package modules

import javax.inject.{Inject, Provider}
import play.api.Configuration

case class StravaConfig(pageSize: Int)

object StravaConfig {

  def apply(configuration: Configuration): StravaConfig = {
    new StravaConfig(
      configuration.get[Int]("strava.pageSize")
    )
  }

}

class StravaConfigProvider @Inject()(configuration: Configuration) extends Provider[StravaConfig] {
  override def get(): StravaConfig = StravaConfig(configuration)
}
