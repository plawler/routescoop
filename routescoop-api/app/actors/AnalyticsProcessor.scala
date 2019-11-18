package actors

import javax.inject.Inject
import models.{PowerEffortsCreated, StravaDataSyncCompleted, UserSettingsCreated}
import modules.NonBlockingContext
import services.PowerAnalysisService

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class AnalyticsProcessor @Inject()(
  analysisService: PowerAnalysisService
)(implicit @NonBlockingContext ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive = {
    case msg: StravaDataSyncCompleted =>
      log.info(s"Starting power effort calculations for all activities with sync id ${msg.syncId}")
      analysisService.createPowerEfforts(msg.syncId) map (_ => log.info("Completed all power efforts"))
    case msg: PowerEffortsCreated =>
      log.info(s"Creating activity stats for activity ${msg.activity.id}")
      analysisService.createActivityStats(msg.activity) map { _ =>
        log.info(s"Activity stats created for ${msg.activity.id}")
      } recover {
        case NonFatal(e) => log.info(s"activity stats not saved for ${msg.activity.id} cause $e")
      }
    case msg: UserSettingsCreated =>
      log.info(s"new user settings created for ${msg.settings.userId}, reprocessing activity stats...")
      analysisService.recalculateActivityStats(msg.settings) recover {
        case NonFatal(e) => log.error(s"activity stats recalculation failed with $e")
      }
    case msg => log.error(s"Cannot process message $msg")
  }

}
