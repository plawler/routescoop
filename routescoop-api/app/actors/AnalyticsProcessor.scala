package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import models.{PowerEffortsCreated, StravaStreamsCreated}
import modules.NonBlockingContext
import services.PowerAnalysisService

import scala.concurrent.ExecutionContext

class AnalyticsProcessor @Inject()(service: PowerAnalysisService)(implicit @NonBlockingContext ec: ExecutionContext)
  extends Actor with ActorLogging {

  override def receive = {
    case msg: StravaStreamsCreated => {
      log.info(s"Creating power efforts for activity ${msg.activity.id}")
      val efforts = service.createEfforts(msg.activity)
      service.saveEfforts(efforts)
      context.system.eventStream.publish(PowerEffortsCreated(msg.activity))
    }
    case msg => log.error(s"Cannot process message $msg")
  }

}
