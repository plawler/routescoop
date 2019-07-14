package controllers

import javax.inject.{Inject, Singleton}
import models.{CriticalPowerResultError, CriticalPowerResultSuccess}
import services.FitnessService

import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class PowerProfile @Inject()(
  fitnessService: FitnessService,
  authenticated: AuthenticatedAction,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  val CP_LOOKBACK_DAYS = 90
  val CP_DURATIONS = Seq(180, 360, 720)

  def index = authenticated.async { implicit request =>
    fitnessService.criticalPower(request.profile.toUser, CP_LOOKBACK_DAYS, CP_DURATIONS) map {
      case CriticalPowerResultSuccess(cp) =>
        Ok(views.html.power.index(cp))
      case CriticalPowerResultError(message) =>
        Logger.error(message)
        Ok(message)
    }
  }

}
