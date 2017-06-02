package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models.{StravaActivityCreated, StravaActivitySyncCompleted, StravaDataSyncCompleted, StravaDataSyncStarted}


class EventStreamRegistry @Inject()(
  actorSystem: ActorSystem,
  @Named("strava-activity-processor") stravaActivityProcessor: ActorRef,
  @Named("strava-datasync-processor") stravaDataSyncProcessor: ActorRef) {

  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaDataSyncStarted])
  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaActivityCreated])
  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaActivitySyncCompleted])

  actorSystem.eventStream.subscribe(stravaDataSyncProcessor, classOf[StravaDataSyncCompleted])

}
