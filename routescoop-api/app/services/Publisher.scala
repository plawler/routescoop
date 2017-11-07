package services

import javax.inject.Inject

import akka.actor.ActorSystem
import models.PowerEffortsCreated

class Publisher @Inject()(actorSystem: ActorSystem) {

  def publish(event: PowerEffortsCreated): Unit = {
    actorSystem.eventStream.publish(event)
  }

}
