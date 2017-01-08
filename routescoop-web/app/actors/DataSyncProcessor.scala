package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import models.StravaDataSyncRequest
import services.DataSyncService


class DataSyncProcessor @Inject()(service: DataSyncService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case StravaDataSyncRequest(token, athleteId) =>
      log.debug("received the data synchronization request")
      service.fetchStravaData(token, athleteId)
    case _ => log.debug("i do not know how to process this message")
  }

}
