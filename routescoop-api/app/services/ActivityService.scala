package services

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}
import models._
import modules.NonBlockingContext
import repositories.{StravaActivityStore, StravaLapStore, StravaStreamStore}

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

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
    logger.info(s"Synching activities for user $userDataSync")
    val userId = userDataSync.userId

    stravaWebService.getActivities(userId) map { stravaActivities =>
      logger.info(s"Available activities count is ${stravaActivities.size}")
      val unprocessedActivities = filterLatest(userId, stravaActivities)
      logger.info(s"Unprocessed activities count is ${unprocessedActivities.size}")
      unprocessedActivities.foreach { activity =>
        val activityToSync = activity.copy(dataSyncId = Some(userDataSync.id))
        saveActivity(activityToSync)
      }
      unprocessedActivities.size
    }
  }

  override def getActivity(activityId: String): Future[Option[StravaActivity]] = Future {
    blocking {
      activityStore.findById(activityId)
    }
  }

  override def syncActivityDetails(activity: StravaActivity): Future[Unit] = {
    for {
      _ <- syncLaps(activity)
      _ <- syncStreams(activity)
    } yield actorSystem.eventStream.publish(StravaActivitySyncCompleted(activity))
  }

  private def saveActivity(activity: StravaActivity) = {
    activityStore.insert(activity)
    actorSystem.eventStream.publish(StravaActivityCreated(activity))
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