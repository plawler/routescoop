package controllers

import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}
import modules.{AppConfig, NonBlockingContext}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.ActivityService

import scala.concurrent.ExecutionContext

@Singleton
class Activities @Inject()(activityService: ActivityService, config: AppConfig)(implicit @NonBlockingContext ec: ExecutionContext)
  extends Controller with LazyLogging {

  def list(userId: String, page: Int) = Action.async { implicit request =>
    activityService.fetchActivities(userId, page, config.pageSize) map { summaries =>
      Ok(Json.toJson(summaries))
    }
  }

}
