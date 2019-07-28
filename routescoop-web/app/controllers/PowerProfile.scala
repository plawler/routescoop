package controllers

import javax.inject.{Inject, Singleton}
import models.{PowerProfileResultError, PowerProfileResultSuccess}
import services.FitnessService

import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class PowerProfile @Inject()(
  fitnessService: FitnessService,
  authenticated: AuthenticatedAction,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  val LOOKBACK_DAYS = 90
  val CP_DURATIONS = Seq(180, 360, 720)
  val DISPLAY_DURATIONS = Seq(1, 15, 30, 60, 120, 180, 240, 300, 600, 1200, 1800, 2700, 3600, 7200, 9000, 1800)

  def index(days: Option[Int]) = authenticated.async { implicit request =>
    fitnessService.powerProfile(request.profile.toUser, days.getOrElse(LOOKBACK_DAYS), CP_DURATIONS) map {
      case PowerProfileResultSuccess(pp) =>
        Ok(views.html.power.index(pp.cp, pp.mmp))
      case PowerProfileResultError(message) =>
        Logger.error(message)
        Ok(message)
    }
  }

  def profile(days: Option[Int]) = authenticated.async { implicit request =>
    fitnessService.powerProfile(request.profile.toUser, days.getOrElse(LOOKBACK_DAYS), CP_DURATIONS) map {
      case PowerProfileResultSuccess(pp) =>
        Ok(Json.toJson(pp))
      case PowerProfileResultError(message) =>
        Logger.error(message)
        Ok(message)
    }
  }

}
