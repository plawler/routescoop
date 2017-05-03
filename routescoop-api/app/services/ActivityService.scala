package services

import modules.NonBlockingContext
import repositories.{StravaActivityStore, StravaLapStore, StravaStreamStore}
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}

import akka.actor.Status.Success
import com.typesafe.scalalogging.LazyLogging
import models._

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.language.postfixOps
import scala.util.Failure

trait ActivityService {

  def syncActivities(userId: String): Future[Unit]

  def syncActivityData(activityId: String): Future[Unit]

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

  override def syncActivities(userId: String): Future[Unit] = {
    stravaService.getActivities(userId).map { activities =>
      filterLatest(userId, activities).foreach { activity =>
        activityStore.insert(activity)
        actorSystem.eventStream.publish(StravaActivityCreated(activity))
      }
    }
  }

  override def getActivity(activityId: String): Future[Option[StravaActivity]] = Future {
    blocking { activityStore.findById(activityId) }
  }

  override def syncActivityData(activityId: String): Future[Unit] = {
    val f1 = syncLaps(activityId)
    val f2 = syncStreams(activityId)
    for {
      _ <- f1
      _ <- f2
    } yield actorSystem.eventStream.publish(StravaActivitiesSynched)
  }

  override def syncLaps(activityId: String): Future[Unit] = {
    // fetch all laps for an activity and send collection to lap store
    getActivity(activityId) flatMap {
      case Some(activity) => {
        stravaService.getLaps(activity) map { laps =>
          laps.foreach(lapStore.insert)
          actorSystem.eventStream.publish(StravaLapsCreated(activity))
        }
      }
      case None => Future.successful(logger.info(s"No activity found for id $activityId"))
    }
  }

  override def syncStreams(activityId: String): Future[Unit] = {
    getActivity(activityId) flatMap {
      case Some(activity) => {
        stravaService.getStreams(activity) map { streams =>
          streamStore.insertBatch(streams)
          actorSystem.eventStream.publish(StravaStreamsCreated(activity))
        }
      }
      case None => Future.successful(logger.info(s"No activity found with id $activityId"))
    }
  }

  private def filterLatest(userId: String, stravaActivities: Seq[StravaActivity]) = {
    val localActivities = activityStore.findByUserId(userId)
    stravaActivities.filterNot(a => localActivities.exists(a.stravaId == _.stravaId))
  }

}
