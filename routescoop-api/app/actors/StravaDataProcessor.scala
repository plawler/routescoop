package actors

import models.StravaDataSyncStarted

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}

import javax.inject.Inject

class StravaDataProcessor @Inject()() extends Actor with ActorLogging {
  override def receive: Receive = {
    case started: StravaDataSyncStarted =>

  }
}
