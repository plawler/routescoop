package actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import models._


class EventStreamRegistry @Inject()(
  actorSystem: ActorSystem,
  @Named("strava-activity-processor") stravaActivityProcessor: ActorRef,
  @Named("strava-datasync-processor") stravaDataSyncProcessor: ActorRef,
  @Named("analytics-processor") analyticsProcessor: ActorRef) {

  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaDataSyncStarted])
  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaActivityCreated])
  actorSystem.eventStream.subscribe(stravaActivityProcessor, classOf[StravaActivitySyncCompleted])
  actorSystem.eventStream.subscribe(stravaDataSyncProcessor, classOf[StravaDataSyncCompleted])

  actorSystem.eventStream.subscribe(analyticsProcessor, classOf[StravaDataSyncCompleted])
  actorSystem.eventStream.subscribe(analyticsProcessor, classOf[PowerEffortsCreated])
}
