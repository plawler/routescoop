package actors

import models.{StravaActivityCreated, StravaDataSyncStarted}
import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject

import services.ActivityService


class StravaDataProcessor @Inject()(activityService: ActivityService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case started: StravaDataSyncStarted =>
      log.info(s"User data sync initiated for ${started.sync.userId}. Getting strava activities...")
      activityService.syncActivities(started.sync.userId)
    case activityCreated: StravaActivityCreated =>
      log.info(s"Completed processing of activity ${activityCreated.activity}")
      activityService.syncLaps(activityCreated.activity.id)
      // streamsService.syncStreams(activity)
    case _ => log.info("wut???")
  }

}
