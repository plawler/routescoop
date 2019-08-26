package actors

import javax.inject.Inject
import models.{PowerEffortsCreated, StravaDataSyncCompleted, UserSettingsCreated}
import modules.NonBlockingContext
import services.{ActivityService, PowerAnalysisService}

import akka.actor.{Actor, ActorLogging}

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class AnalyticsProcessor @Inject()(
  activityService: ActivityService,
  analysisService: PowerAnalysisService
)(implicit @NonBlockingContext ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive = {
    case msg: StravaDataSyncCompleted =>
      log.info(s"Starting power effort calculations for all activities with sync id ${msg.syncId}")
      activityService.getActivitiesBySync(msg.syncId) map { activities =>
        activities.foreach { activity =>
          val efforts = analysisService.calculatePowerEfforts(activity)
          analysisService.savePowerEfforts(efforts)
          context.system.eventStream.publish(PowerEffortsCreated(activity))
        }
      } map (_ => log.info("Completed power effort calculations"))
    case msg: PowerEffortsCreated =>
      log.info(s"Creating activity stats for activity ${msg.activity.id}")
      analysisService.createActivityStats(msg.activity) map {
        case Some(stats) => analysisService.saveActivityStats(stats)
        case None => log.info(s"Activity stats not saved for ${msg.activity.id}")
      }
    case msg: UserSettingsCreated =>
      log.info(s"new user settings created for ${msg.settings.userId}, reprocessing activity stats...")
      analysisService.recalculateActivityStats(msg.settings) recover {
        case NonFatal(e) => log.error(s"activity stats recalculation failed with $e")
      }
    case msg => log.error(s"Cannot process message $msg")
  }

}
