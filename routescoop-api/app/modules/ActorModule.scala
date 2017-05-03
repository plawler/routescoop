package modules

import actors.{EventStreamRegistry, StravaActivityProcessor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[StravaActivityProcessor]("strava-activity-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()
  }

}
