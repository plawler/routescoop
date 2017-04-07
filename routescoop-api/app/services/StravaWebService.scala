package services

import java.time.Instant
import java.util.UUID
import javax.inject.Inject

import com.fasterxml.uuid.Generators
import com.google.inject.Singleton
import kiambogo.scrava.ScravaClient
import kiambogo.scrava.models.{LapEffort, PersonalActivitySummary}
import models.{StravaActivity, StravaLap, User}
import modules.NonBlockingContext

import scala.concurrent.{ExecutionContext, Future}


trait StravaWebService {

  def getActivities(userId: String): Future[Seq[StravaActivity]]

  def getLaps(activity: StravaActivity): Future[Seq[StravaLap]]

}

@Singleton
class ScravaWebService @Inject()(userService: UserService)(implicit @NonBlockingContext ec: ExecutionContext)
    extends StravaWebService {

  override def getActivities(userId: String): Future[Seq[StravaActivity]] = {
    userService.getUser(userId).map {
      case Some(user) => getUserActivities(user)
      case None => Nil
    }
  }

  override def getLaps(activity: StravaActivity): Future[Seq[StravaLap]] = {
    userService.getUser(activity.userId) map {
      case Some(user) =>
        user.stravaToken match {
          case Some(token) =>
            getClient(token).listActivityLaps(activity.stravaId).map(lap => StravaLap.create(activity, lap))
          case None => Nil
        }
      case None => Nil
    }
  }

  private def getUserActivities(user: User): Seq[StravaActivity] = {
    user.stravaToken match {
      case Some(token) => getClient(token).listAthleteActivities().map(summary => StravaActivity.create(user, summary))
      case None => Nil
    }
  }

  private def getClient(token: String) = new ScravaClient(token)

}
