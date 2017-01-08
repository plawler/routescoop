package modules

import actors.{EventStreamRegistry, DataSyncProcessor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * Created by paullawler on 12/10/16.
  */
class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DataSyncProcessor]("data-sync-processor")
    bind(classOf[EventStreamRegistry]).asEagerSingleton()
  }

}
