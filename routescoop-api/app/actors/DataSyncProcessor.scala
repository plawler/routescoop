package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import models.DataSyncRequest
import services.DataSyncService


class DataSyncProcessor @Inject()(service: DataSyncService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case request: DataSyncRequest =>
      log.debug("received the data synchronization request")
      service.syncUserData(request)
    case _ => log.error(s"Cannot process message")
  }

}