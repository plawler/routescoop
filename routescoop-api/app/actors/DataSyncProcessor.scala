package actors

import models.UserDataSyncRequest
import services.DataSyncService

import akka.actor.{Actor, ActorLogging}

import javax.inject.Inject


class DataSyncProcessor @Inject()(service: DataSyncService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case request: UserDataSyncRequest =>
      log.debug("received the data synchronization request")
      service.startDataSync(request)
    case "DataSyncCompleted" =>
      // service.completeDataSync(completed.id)
      println("finished synching user data")
    case _ => log.error(s"Cannot process message")
  }

}
