package controllers

import ejisan.play.libs.{PageMetaApi, PageMetaSupport}
import javax.inject.{Inject, Singleton}
import models.Profile
import play.api.Logger
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller


@Singleton
class User @Inject()(AuthenticatedAction: AuthenticatedAction,
                     cache: CacheApi,
                     val messagesApi: MessagesApi,
                     val pageMetaApi: PageMetaApi,
                     implicit val wja: WebJarAssets) extends Controller with I18nSupport with PageMetaSupport {

  def profile = AuthenticatedAction { implicit request =>
    request.session.get("idToken") flatMap { id =>
      cache.get[Profile](id + "profile") map { profile =>
        Logger.debug(s"User profile is: $profile")
        Ok(views.html.user.profile(profile))
      }
    } getOrElse Redirect(routes.Auth.login())
  }

}
