package controllers

import javax.inject.Inject
import modules.NonBlockingContext
import play.api.mvc.{Action, Controller}
import services.HealthService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class Application @Inject()(healthService: HealthService)(implicit @NonBlockingContext ec: ExecutionContext)
  extends Controller {

  def ping = Action { implicit request =>
    Ok("OK")
  }

  def health = Action.async { implicit request =>
    try {
      if (healthService.isDatabaseHealthy) {
        Future.successful(Ok("healthy"))
      } else Future.successful(ServiceUnavailable("routescoop-api: database down"))
    } catch {
      case NonFatal(e) => Future.successful(ServiceUnavailable("routescoop-api: database down"))
    }
  }

}
