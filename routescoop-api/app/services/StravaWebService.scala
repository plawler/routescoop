package services

import javax.inject.Inject

import com.google.inject.Singleton
import com.typesafe.scalalogging.LazyLogging
import kiambogo.scrava.ScravaClient
import kiambogo.scrava.models.Streams
import models.{StravaActivity, StravaLap, StravaStream, User}
import modules.NonBlockingContext

import scala.concurrent.{ExecutionContext, Future}


trait StravaWebService {

  def getActivities(userId: String): Future[Seq[StravaActivity]]

  def getLaps(activity: StravaActivity): Future[Seq[StravaLap]]

  def getStreams(activity: StravaActivity): Future[Seq[StravaStream]]

}

@Singleton
class ScravaWebService @Inject()(userService: UserService)(implicit @NonBlockingContext ec: ExecutionContext)
  extends StravaWebService with LazyLogging {

  override def getActivities(userId: String): Future[Seq[StravaActivity]] = {
    userService.getUser(userId).map {
      case Some(user) =>
        logger.info("User exists. Fetching activities from Strava...")
        getUserActivities(user)
      case None => Nil
    }
  }

  override def getLaps(activity: StravaActivity): Future[Seq[StravaLap]] = {
    userService.getUser(activity.userId) map {
      case Some(user) =>
        user.stravaToken match {
          case Some(token) =>
            createClient(token).listActivityLaps(activity.stravaId).map(lap => StravaLap.create(activity, lap))
          case None => Nil
        }
      case None => Nil
    }
  }

  override def getStreams(activity: StravaActivity): Future[Seq[StravaStream]] = {
    getWebClient(activity.userId) map {
      case Some(client) =>
        val streams = client.retrieveActivityStream(activity.stravaId.toString) // todo: handle exception if record not found
        pivotThenMap(streams) map (raw => StravaStream.createFromScrava(activity, raw))
      case None => Nil
    }
  }

  private def getUserActivities(user: User): Seq[StravaActivity] = {
    user.stravaToken match {
      case Some(token) =>
        val things = createClient(token).listAthleteActivities()
        logger.info(s"Found ${things.size} athlete activities")
        things.map(summary => StravaActivity.create(user, summary))
      case None => Nil
    }
  }

  private def createClient(token: String) = new ScravaClient(token)

  private def getWebClient(userId: String): Future[Option[ScravaClient]] = {
    userService.getUser(userId) map {
      case Some(user) =>
        user.stravaToken match {
          case Some(token) => Some(createClient(token))
          case _ => None
        }
      case _ => None
    }
  }

  private def getUser(userId: String) = userService.getUser(userId)

  private def pivotThenMap(streams: List[Streams]): List[Map[String, Any]] = { // todo break up into 2 functions
    val types = streams map (_.`type`)
    val data = streams map (_.data)
    val pivoted = data.transpose
    pivoted map (values => (types zip values).toMap)
  }

}
