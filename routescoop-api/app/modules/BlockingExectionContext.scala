package modules

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

class BlockingExectionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "akka.actor.blocking-dispatcher") {
}
