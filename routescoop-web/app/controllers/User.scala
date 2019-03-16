package controllers

import javax.inject.{Inject, Singleton}
import models.Profile
import play.api.Logger
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller


@Singleton
class User @Inject()(authenticated: AuthenticatedAction,
                     cache: CacheApi,
                     val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def profile = authenticated { implicit request =>
    Ok(views.html.user.profile(request.profile))
  }

}
