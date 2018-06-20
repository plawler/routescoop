package modules

import com.google.inject.{AbstractModule, Provides}
import config._
import controllers.AuthenticatedAction

import scala.concurrent.ExecutionContext

class ApplicationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AuthConfig]).toProvider(classOf[AuthConfigProvider])
    bind(classOf[StravaConfig]).toProvider(classOf[StravaConfigProvider])
    bind(classOf[AppConfig]).toProvider(classOf[AppConfigProvider])
    bind(classOf[AuthenticatedAction])
  }

  @Provides @NonBlockingContext
  def getNonBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
}
