package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models.StravaDataSyncStarted


class EventStreamRegistry @Inject()(
  actorSystem: ActorSystem,
  @Named("strava-data-processor") stravaDataActor: ActorRef) {

  actorSystem.eventStream.subscribe(stravaDataActor, classOf[StravaDataSyncStarted])

}
