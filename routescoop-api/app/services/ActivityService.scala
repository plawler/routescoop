package services

import modules.NonBlockingContext
import repositories.{StravaActivityStore, StravaLapStore}
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}

import models.{StravaActivity, StravaActivityCreated, StravaLapsCreated}

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.language.postfixOps

trait ActivityService {

  def syncActivities(userId: String): Future[Unit]

  def getActivity(activityId: String): Future[Option[StravaActivity]]

  def syncLaps(activityId: String): Future[Unit]

}

@Singleton
class StravaActivityService @Inject()(
  stravaService: StravaWebService,
  activityStore: StravaActivityStore,
  lapStore: StravaLapStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends ActivityService {

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

  override def syncLaps(activityId: String): Future[Unit] = {
    // fetch all laps for an activity and send collection to lap store
    getActivity(activityId).map {
      case Some(activity) => {
        stravaService.getLaps(activity).map(_.foreach(lapStore.insert))
        actorSystem.eventStream.publish(StravaLapsCreated(activity))
      }
      case None => println(s"no activity found for id $activityId")
    }
  }

  private def filterLatest(userId: String, stravaActivities: Seq[StravaActivity]) = {
    val localActivities = activityStore.findByUserId(userId)
    stravaActivities.filterNot(a => localActivities.exists(a.stravaId == _.stravaId))
  }

}
