package modules

import actors.{DataSyncProcessor, EventStreamRegistry}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DataSyncProcessor]("data-sync-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()
  }

}
