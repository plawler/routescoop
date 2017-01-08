package modules

import com.google.inject.{AbstractModule, Provides}
import config.{AuthConfig, AuthConfigProvider, StravaAccessConfig, StravaAccessConfigProvider}
import controllers.AuthenticatedAction
import services.{StravaDataSyncService, DataSyncService}

import scala.concurrent.ExecutionContext

class ApplicationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AuthConfig]).toProvider(classOf[AuthConfigProvider])
    bind(classOf[StravaAccessConfig]).toProvider(classOf[StravaAccessConfigProvider])
    bind(classOf[AuthenticatedAction])
    bind(classOf[DataSyncService]).to(classOf[StravaDataSyncService])
  }

  @Provides @NonBlockingContext
  def getNonBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
}
