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
    case msg: StravaStreamsCreated =>
      log.info(s"Creating power efforts for activity ${msg.activity.id}")
      val efforts = service.calculatePowerEfforts(msg.activity)
      service.savePowerEfforts(efforts)
      context.system.eventStream.publish(PowerEffortsCreated(msg.activity))
    case msg: PowerEffortsCreated =>
      log.info(s"Creating activity stats for activity ${msg.activity.id}")
      service.createActivityStats(msg.activity) map {
        case Some(stats) => service.saveActivityStats(stats)
        case None => log.info(s"Activity stats not saved for ${msg.activity.id}")
      }
//      service.createActivityStats(msg.activity) map (_ foreach service.saveActivityStats)
    case msg => log.error(s"Cannot process message $msg")
  }

}
