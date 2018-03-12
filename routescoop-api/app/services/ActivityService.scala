package services

import modules.NonBlockingContext
import repositories.{StravaActivityStore, StravaLapStore, StravaStreamStore}
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}

import com.typesafe.scalalogging.LazyLogging
import models._

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.language.postfixOps

trait ActivityService {

  def syncActivities(userDataSync: UserDataSync): Future[Int]

  def syncActivityDetails(activity: StravaActivity): Future[Unit]

  def getActivity(activityId: String): Future[Option[StravaActivity]]

}

@Singleton
class StravaActivityService @Inject()(
  stravaWebService: StravaWebService,
  activityStore: StravaActivityStore,
  lapStore: StravaLapStore,
  streamStore: StravaStreamStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends ActivityService with LazyLogging {

  override def syncActivities(userDataSync: UserDataSync): Future[Int] = {
    val userId = userDataSync.userId
    stravaWebService.getActivities(userId) map { stravaActivities =>
      val unprocessedActivities = filterLatest(userId, stravaActivities)
      unprocessedActivities.foreach { activity =>
        val activityToSync = activity.copy(dataSyncId = Some(userDataSync.id))
        activityStore.insert(activityToSync)
        actorSystem.eventStream.publish(StravaActivityCreated(activityToSync))
      }
      unprocessedActivities.size
    }
  }

  override def getActivity(activityId: String): Future[Option[StravaActivity]] = Future {
    blocking { activityStore.findById(activityId) }
  }

  override def syncActivityDetails(activity: StravaActivity): Future[Unit] = {
    val f1 = syncLaps(activity)
    val f2 = syncStreams(activity)
    for {
      _ <- f1
      _ <- f2
    } yield actorSystem.eventStream.publish(StravaActivitySyncCompleted(activity))
  }

  private def syncLaps(activity: StravaActivity): Future[Unit] = {
    // fetch all laps for an activity and send collection to lap store
    stravaWebService.getLaps(activity) map { laps =>
      laps.foreach(lapStore.insert) // todo: insertBatch
      actorSystem.eventStream.publish(StravaLapsCreated(activity))
    }
  }

  private def syncStreams(activity: StravaActivity): Future[Unit] = {
    stravaWebService.getStreams(activity) map { streams =>
      streamStore.insertBatch(streams)
      actorSystem.eventStream.publish(StravaStreamsCreated(activity))
    }
  }

  private def filterLatest(userId: String, stravaActivities: Seq[StravaActivity]) = {
    val localActivities = activityStore.findByUserId(userId)
    stravaActivities.filterNot(a => localActivities.exists(a.stravaId == _.stravaId))
  }

}
