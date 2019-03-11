package modules

import com.google.inject.{AbstractModule, Provides}
import repositories._
import services._

import scala.concurrent.ExecutionContext


class ApplicationModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[UserStore]).to(classOf[UserSqlStore])
    bind(classOf[UserDataSyncStore]).to(classOf[UserDataSyncSqlStore])
    bind(classOf[UserSettingsStore]).to(classOf[UserSettingsSqlStore])
    bind(classOf[UserService]).to(classOf[UserServiceImpl])

    bind(classOf[DataSyncService]).to(classOf[StravaDataSyncService])

    bind(classOf[AppConfig]).toProvider(classOf[AppConfigProvider])
    bind(classOf[StravaConfig]).toProvider(classOf[StravaConfigProvider])
    bind(classOf[StravaActivityStore]).to(classOf[StravaActivityStoreImpl])
    bind(classOf[StravaLapStore]).to(classOf[StravaLapStoreImpl])
    bind(classOf[StravaStreamStore]).to(classOf[StravaStreamStoreImpl])
    bind(classOf[ActivityService]).to(classOf[StravaActivityService])

    bind(classOf[PowerEffortStore]).to(classOf[PowerEffortStoreImpl])
    bind(classOf[ActivityStatsStore]).to(classOf[ActivityStatsStoreSql])
    bind(classOf[PowerAnalysisService])

    bind(classOf[StravaWebService]).to(classOf[StravaWebServiceImpl])
  }

  @Provides @NonBlockingContext
  def getNonBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  @Provides @BlockingContext
  def getBlockingContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

}
