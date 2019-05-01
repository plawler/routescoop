package controllers

import javax.inject.Inject
import modules.NonBlockingContext
import services.HealthService

import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class Application @Inject()(healthService: HealthService, val controllerComponents: ControllerComponents)
  (implicit @NonBlockingContext ec: ExecutionContext) extends BaseController {

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
