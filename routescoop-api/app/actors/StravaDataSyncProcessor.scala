package actors

import javax.inject.Inject

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import models.StravaDataSyncCompleted
import modules.NonBlockingContext
import services.UserService

import scala.concurrent.ExecutionContext

/**
  * Created by paullawler on 5/5/17.
  */
class StravaDataSyncProcessor @Inject()(userService: UserService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case dsc: StravaDataSyncCompleted =>
      userService.completeDataSync(dsc.syncId, dsc.completedAt)
    case _ => log.info("unable to process message")
  }

}
