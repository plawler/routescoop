package modules

import actors.{EventStreamRegistry, StravaActivityProcessor, StravaDataSyncProcessor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[StravaActivityProcessor]("strava-activity-processor")
    bindActor[StravaDataSyncProcessor]("strava-datasync-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()
  }

}
