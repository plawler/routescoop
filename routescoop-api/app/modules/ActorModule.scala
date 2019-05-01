package modules

import actors.{AnalyticsProcessor, EventStreamRegistry, StravaActivityProcessor, StravaDataSyncProcessor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.Publisher


class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[StravaActivityProcessor]("strava-activity-processor")
    bindActor[StravaDataSyncProcessor]("strava-datasync-processor")
    bindActor[AnalyticsProcessor]("analytics-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()

    bind(classOf[Publisher])
  }

}

