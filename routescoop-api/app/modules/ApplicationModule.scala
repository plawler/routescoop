package modules

import com.google.inject.{AbstractModule, Provides}
import repositories.{DataSyncRequestSqlStore, DataSyncRequestStore, UserSqlStore, UserStore}
import services.{DataSyncService, StravaSyncService, UserService, UserServiceImpl}

import scala.concurrent.ExecutionContext


class ApplicationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[UserStore]).to(classOf[UserSqlStore])
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[DataSyncRequestStore]).to(classOf[DataSyncRequestSqlStore])
    bind(classOf[DataSyncService]).to(classOf[StravaSyncService])
  }

  @Provides @NonBlockingContext
  def getNonBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  @Provides @BlockingContext
  def getBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

}
