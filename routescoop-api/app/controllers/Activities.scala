package controllers

import javax.inject.{Inject, Singleton}
import models.ActivityStats
import modules.{AppConfig, NonBlockingContext}
import services.{ActivityService, PowerAnalysisService}

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import java.sql.{SQLException, SQLIntegrityConstraintViolationException}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

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

  def updateStats = Action.async(parse.json) { implicit request =>
    request.body.validate[ActivityStats].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      stats => {
        activityService.getActivity(stats.activityId) map {
          case Some(_) =>
            powerAnalysisService.updateActivityStats(stats)
            NoContent
          case None => BadRequest(s"Invalid activity stats ${Json.toJson(stats)}")
        }
      }
    )
  }

  def getPowerDistribution(activityId: String) = Action.async { implicit request =>
    Future.successful(Ok("activity power distribution"))
  }

  def generateInZoneStats(activityId: String) = Action.async { implicit request =>
    powerAnalysisService.generateTimeInZoneStats(activityId) map (stats => Ok(Json.toJson(stats)))
  }

}
