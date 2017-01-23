package modules

import actors.{EventStreamRegistry, StravaDataProcessor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[StravaDataProcessor]("strava-data-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()
  }

}
