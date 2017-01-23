package modules

import com.google.inject.{AbstractModule, Provides}
import repositories._
import services._

import scala.concurrent.ExecutionContext


class ApplicationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[UserStore]).to(classOf[UserSqlStore])
    bind(classOf[UserDataSyncStore]).to(classOf[UserDataSyncSqlStore])
    bind(classOf[UserService]).to(classOf[UserServiceImpl])

    bind(classOf[StravaActivityStore]).to(classOf[StravaActivityStoreImpl])
    bind(classOf[StravaWebService]).to(classOf[ScravaWebService])
    bind(classOf[ActivityService]).to(classOf[StravaActivityService])
  }

  @Provides @NonBlockingContext
  def getNonBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  @Provides @BlockingContext
  def getBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

}
