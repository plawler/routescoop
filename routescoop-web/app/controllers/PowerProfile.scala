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

  def powerCurve(days: Option[Int]) = authenticated.async { implicit request =>
    fitnessService.powerProfile(request.profile.toUser, days.getOrElse(LOOKBACK_DAYS), CP_DURATIONS) map {
      case PowerProfileResultSuccess(pp) =>
        val mmpData = pp.mmp.efforts map (effort => ChartXY(effort.duration, effort.watts))
        val cpData = pp.cp.predictions map (effort => ChartXY(effort.duration, effort.watts))
        Ok(views.html.power.mmp(mmpData, cpData, pp.cp))
      case PowerProfileResultError(message) =>
        Logger.error(message)
        Ok(message)
    }
  }

}
