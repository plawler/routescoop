package controllers

import javax.inject.Inject

import com.typesafe.scalalogging.LazyLogging
import modules.NonBlockingContext
import play.api.mvc.{Action, Controller}
import services.StravaWebService

import scala.concurrent.ExecutionContext

class Activities @Inject()(stravaWebService: StravaWebService)(implicit @NonBlockingContext ec: ExecutionContext)
  extends Controller with LazyLogging {

  def list(userId: String) = Action.async { implicit request =>
    stravaWebService.getActivities(userId) map { activities =>
      Ok(s"Did you see the list of $activities") // todo: use local activity service
    }
  }

}
