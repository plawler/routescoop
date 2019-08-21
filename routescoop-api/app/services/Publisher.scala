package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models.{PowerEffortsCreated, UserSettingsCreated}

@Singleton
class Publisher @Inject()(actorSystem: ActorSystem) {

  def publish(event: PowerEffortsCreated): Unit = {
    actorSystem.eventStream.publish(event)
  }

  def publish(event: UserSettingsCreated): Unit = {
    actorSystem.eventStream.publish(event)
  }

}
