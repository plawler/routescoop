package controllers

import javax.inject._
import models.{FitnessTrendResultError, FitnessTrendResultSuccess}
import services.FitnessService

import play.api.Logger
import play.api.i18n._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Home @Inject()(
  fitnessService: FitnessService,
  authenticated: AuthenticatedAction,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = authenticated.async { implicit request =>
    fitnessService.fitnessTrend(request.profile.toUser, 180) map {
      case FitnessTrendResultSuccess(trend) =>
        Ok(views.html.index(trend))
      case FitnessTrendResultError(message) =>
        Logger.error(message)
        Ok(views.html.index(Seq())).flashing("error" -> "Failed to retrieve the fitness data")
    }
  }

  def bootstrap = Action { implicit request =>
    Ok(views.html.test.bootstrap())
  }

}
