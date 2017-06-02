package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import models.{StravaActivityCreated, StravaActivitySyncCompleted, StravaDataSyncCompleted, StravaDataSyncStarted}
import modules.NonBlockingContext
import services.ActivityService

import scala.concurrent.ExecutionContext

case object ActivityProcessed

case class ActivityProcessingCompleted(syncId: String)

class ActivityCountMonitor(totalCount: Int) extends Actor with ActorLogging {

  val syncId = self.path.name
  var currentCount = 0

  log.info(s"creating the activity count monitor for $syncId...waiting for $totalCount activities to be synched")

  override def receive: Receive = {
    case ActivityProcessed =>
      currentCount = currentCount + 1
      log.info(s"current activity count is $currentCount out of $totalCount")
      if (currentCount == totalCount) context.parent ! ActivityProcessingCompleted(syncId)
    case _ => log.error("encountered an unknown message")
  }

  override def postStop(): Unit = log.info("my work is done here. goodbye.")

}


class StravaActivityProcessor @Inject()(activityService: ActivityService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case started: StravaDataSyncStarted =>
      log.info(s"User data sync initiated for ${started.sync.userId}. Getting strava activities...")
      activityService.syncActivities(started.sync) map { count =>
        context.actorOf(Props(new ActivityCountMonitor(count)), started.sync.id)
      }
    case created: StravaActivityCreated =>
      log.info(s"Getting activity data for activity ${created.activity.id}")
      activityService.syncActivity(created.activity)
    case synched: StravaActivitySyncCompleted =>
      synched.activity.dataSyncId foreach { syncId =>
        context.child(syncId) foreach (_ ! ActivityProcessed)
      }
    case completed: ActivityProcessingCompleted =>
      log.info(s"Finished processing all activities for user data sync ${completed.syncId}")
      context.system.eventStream.publish(StravaDataSyncCompleted(completed.syncId))
      context.child(completed.syncId) foreach context.stop
    case _ => log.info("unable to process message")
  }

}
