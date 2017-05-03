package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models.{StravaActivityCreated, StravaDataSyncStarted}


class EventStreamRegistry @Inject()(
  actorSystem: ActorSystem,
  @Named("strava-activity-processor") stravaActivityProcessor: ActorRef) {

  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaDataSyncStarted])
  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaActivityCreated])

}
