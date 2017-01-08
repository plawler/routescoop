package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models.StravaDataSyncRequest

// bound as an eager singleton in actor module
class EventStreamRegistry @Inject()(actorSystem: ActorSystem, @Named("data-sync-processor") dataSyncActor: ActorRef) {

  actorSystem.eventStream.subscribe(dataSyncActor, classOf[StravaDataSyncRequest])

}
