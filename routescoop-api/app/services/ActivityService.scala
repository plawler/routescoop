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

  def syncActivities(userId: String): Future[Int]
  def syncActivities(userDataSync: UserDataSync): Future[Int]

  def syncActivity(activityId: String): Future[Unit]
  def syncActivity(activity: StravaActivity): Future[Unit]

  def syncLaps(activityId: String): Future[Unit]

  def syncStreams(activityId: String): Future[Unit]

  def getActivity(activityId: String): Future[Option[StravaActivity]]

}

@Singleton
class StravaActivityService @Inject()(
  stravaService: StravaWebService,
  activityStore: StravaActivityStore,
  lapStore: StravaLapStore,
  streamStore: StravaStreamStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends ActivityService with LazyLogging {

  override def syncActivities(userId: String): Future[Int] = {
    stravaService.getActivities(userId) map { activities =>
      filterLatest(userId, activities).foreach { activity =>
        activityStore.insert(activity)
        actorSystem.eventStream.publish(StravaActivityCreated(activity))
      }
      activities.size
    }
  }

  override def syncActivities(userDataSync: UserDataSync): Future[Int] = {
    val userId = userDataSync.userId
    stravaService.getActivities(userId) map { activities =>
      filterLatest(userId, activities).foreach { activity =>
        val activityToSync = activity.copy(dataSyncId = Some(userDataSync.id))
        activityStore.insert(activity) // todo: add the dataSyncId as a foreign key??
        actorSystem.eventStream.publish(StravaActivityCreated(activityToSync))
      }
      activities.size
    }
  }

  override def getActivity(activityId: String): Future[Option[StravaActivity]] = Future {
    blocking { activityStore.findById(activityId) }
  }

  override def syncActivity(activityId: String): Future[Unit] = {
    getActivity(activityId) map {
      case Some(activity) => syncActivity(activity)
      case None => Future.successful(logger.info(s"No activity found for id $activityId"))
    }
  }

  override def syncActivity(activity: StravaActivity): Future[Unit] = {
    val f1 = syncLaps(activity)
    val f2 = syncStreams(activity)
    for {
      _ <- f1
      _ <- f2
    } yield actorSystem.eventStream.publish(StravaActivitySyncCompleted(activity))
  }

  override def syncLaps(activityId: String): Future[Unit] = {
    // fetch all laps for an activity and send collection to lap store
    getActivity(activityId) flatMap {
      case Some(activity) => syncLaps(activity)
      case None => Future.successful(logger.info(s"No activity found for id $activityId"))
    }
  }

  override def syncStreams(activityId: String): Future[Unit] = {
    getActivity(activityId) flatMap {
      case Some(activity) => syncStreams(activity)
      case None => Future.successful(logger.info(s"No activity found with id $activityId"))
    }
  }

  private def syncLaps(activity: StravaActivity): Future[Unit] = {
    // fetch all laps for an activity and send collection to lap store
    stravaService.getLaps(activity) map { laps =>
      laps.foreach(lapStore.insert) // todo: insertBatch
      actorSystem.eventStream.publish(StravaLapsCreated(activity))
    }
  }

  private def syncStreams(activity: StravaActivity): Future[Unit] = {
    stravaService.getStreams(activity) map { streams =>
      streamStore.insertBatch(streams)
      actorSystem.eventStream.publish(StravaStreamsCreated(activity))
    }
  }

  private def filterLatest(userId: String, stravaActivities: Seq[StravaActivity]) = {
    val localActivities = activityStore.findByUserId(userId)
    stravaActivities.filterNot(a => localActivities.exists(a.stravaId == _.stravaId))
  }

}
