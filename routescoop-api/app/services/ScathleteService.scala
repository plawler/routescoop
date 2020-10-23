package services

import javax.inject.{Inject, Singleton}
import models.{StravaActivity, StravaLap, StravaStream}
import modules.NonBlockingContext
import scathlete.clients.StravaClient
import scathlete.models.StravaAccessToken

import com.typesafe.scalalogging.LazyLogging

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScathleteService @Inject()(userService: UserService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends StravaWebService with LazyLogging {

  override def getRecentActivities(userId: String): Future[Seq[StravaActivity]] = {
    getToken(userId) flatMap {
      case Some(accessToken) =>
        val client = new StravaClient(StravaAccessToken(accessToken))
        client.getLatestActivities() map { activities =>
          activities map (a => StravaActivity.create(userId, a))
        } andThen { case _ => client.close() }
      case None => Future.successful(Nil)
    }
  }

  override def getPreviousActivities(userId: String, priorTo: Instant): Future[Seq[StravaActivity]] = {
    getToken(userId) flatMap {
      case Some(accessToken) =>
        val client = new StravaClient(StravaAccessToken(accessToken))
        client.getPreviousActivities(before = priorTo) map { activities =>
          activities map (a => StravaActivity.create(userId, a))
        } andThen { case _ => client.close() }
      case None => Future.successful(Nil)
    }
  }

  override def getLaps(activity: StravaActivity): Future[Seq[StravaLap]] = {
    getToken(activity.userId) flatMap {
      case Some(accessToken) => {
        val client = new StravaClient(StravaAccessToken(accessToken))
        client.getLaps(activity.stravaId) map {
          case Some(laps) => laps map (lap => StravaLap.fromScathleteLap(activity, lap))
          case None => Nil
        } andThen { case _ => client.close() }
      }
      case None =>
        logger.info(s"No laps found for Strava activity: ${activity.stravaId}")
        Future.successful(Nil)
    }
  }

  override def getStreams(activity: StravaActivity): Future[Seq[StravaStream]] = {
    getToken(activity.userId) flatMap {
      case Some(accessToken) => {
        val client = new StravaClient(StravaAccessToken(accessToken))
        client.getActivityStreams(activity.stravaId) map {
          case Some(streams) => streams map (stream => StravaStream.from(stream))
          case None => Nil
        } andThen { case _ => client.close() }
      }
      case None =>
        logger.info(s"No streams fond for Strava activity: ${activity.stravaId}")
        Future.successful(Nil)
    }
  }

  private def getUser(userId: String) = userService.getUser(userId)

  private def getToken(userId: String) = {
    getUser(userId) map {
      case Some(user) => user.stravaToken
      case None => None
    }
  }
}
