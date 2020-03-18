package controllers

import javax.inject.{Inject, Singleton}
import modules.{AppConfig, NonBlockingContext}
import services.{ActivityService, PowerAnalysisService}

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Activities @Inject()(
  activityService: ActivityService,
  powerAnalysisService: PowerAnalysisService,
  config: AppConfig,
  val controllerComponents: ControllerComponents
) (implicit @NonBlockingContext ec: ExecutionContext) extends BaseController with LazyLogging {

  def list(userId: String, page: Int) = Action.async { implicit request =>
    activityService.fetchActivities(userId, page, config.pageSize) map { summaries =>
      Ok(Json.toJson(summaries))
    }
  }

  def getPowerDistribution(activityId: String) = Action.async { implicit request =>
    Future.successful(Ok("activity power distribution"))
  }

  def generateInZoneStats(activityId: String) = Action.async { implicit request =>
    powerAnalysisService.generateTimeInZoneStats(activityId) map (stats => Ok(Json.toJson(stats)))
  }

}
