package services

import modules.NonBlockingContext
import repositories.StravaActivityStore

import akka.actor.ActorSystem

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait ActivityService {

  def syncActivities(userId: String): Future[Unit]

}

@Singleton
class StravaActivityService @Inject()(
  userService: UserService,
  webService: StravaWebService,
  activityStore: StravaActivityStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends ActivityService {

  override def syncActivities(userId: String): Future[Unit] = {
    userService.getUser(userId).map {
      case Some(user) => webService.getLatestActivities(user).foreach(activityStore.insert)
      case None => throw new IllegalArgumentException(s"No user found for $userId")
    }
  }

}
