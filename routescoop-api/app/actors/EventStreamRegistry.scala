package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models.DataSyncRequest


class EventStreamRegistry @Inject()(actorSystem: ActorSystem, @Named("data-sync-processor") dataSyncActor: ActorRef) {

  actorSystem.eventStream.subscribe(dataSyncActor, classOf[DataSyncRequest])

}