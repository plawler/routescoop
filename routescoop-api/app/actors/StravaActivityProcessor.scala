package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import models.{StravaActivityCreated, StravaDataSyncStarted}
import modules.NonBlockingContext
import services.ActivityService

import scala.concurrent.ExecutionContext


class StravaActivityProcessor @Inject()(activityService: ActivityService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case started: StravaDataSyncStarted =>
      log.info(s"User data sync initiated for ${started.sync.userId}. Getting strava activities...")
      activityService.syncActivities(started.sync.userId)
    case activityCreated: StravaActivityCreated =>
      log.info(s"Getting activity data for ${activityCreated.activity}")
      activityService.syncActivityData(activityCreated.activity.id)
    case _ => log.info("wut???")
  }

}
