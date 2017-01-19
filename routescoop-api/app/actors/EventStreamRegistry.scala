package actors

import models.UserDataSyncRequest

import akka.actor.{ActorRef, ActorSystem}

import javax.inject.{Inject, Named}


class EventStreamRegistry @Inject()(actorSystem: ActorSystem, @Named("data-sync-processor") dataSyncActor: ActorRef) {

  actorSystem.eventStream.subscribe(dataSyncActor, classOf[UserDataSyncRequest])

}
