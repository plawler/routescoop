package controllers

import javax.inject.{Inject, Singleton}
import models.Profile

import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext


@Singleton
class User @Inject()(
  authenticated: AuthenticatedAction,
  cache: SyncCacheApi,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  def profile = authenticated { implicit request =>
    request.session.get("idToken") map { idToken =>
      cache.get[Profile](idToken + "profile") match {
        case Some(profile) => Ok(views.html.user.profile(profile))
        case None => Redirect(routes.Auth.logout())
      }
    } getOrElse Redirect(routes.Auth.logout())
  }

}
