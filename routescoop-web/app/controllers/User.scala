package controllers

import javax.inject.{Inject, Singleton}
import com.netaporter.uri.dsl._
import config.AuthConfig
import ejisan.play.libs.{PageMetaApi, PageMetaSupport}
import models.Profile

import play.api.Logger
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, DiscardingCookie}


@Singleton
class User @Inject()(AuthenticatedAction: AuthenticatedAction,
                     cache: CacheApi,
                     config: AuthConfig,
                     val messagesApi: MessagesApi,
                     val pageMetaApi: PageMetaApi,
                     implicit val wja: WebJarAssets) extends Controller with I18nSupport with PageMetaSupport {

  def profile = AuthenticatedAction { implicit request =>
    val cookie = request.cookies.get("idToken").get
    val profile = cache.get[Profile](cookie.value + "profile").get
    Logger.debug(s"User profile is: $profile")
    Ok(views.html.user.profile(profile))
  }

  def login = Action { implicit request =>
    request.cookies.get("idToken").flatMap { cookie =>
      val profileKey = cookie.value + "profile"
      cache.get[Profile](profileKey).map { profile =>
        Logger.debug("authenticated")
        Redirect(routes.Home.index())
      }
    } getOrElse Ok(views.html.login(config))
  }

  def logout = Action { implicit request =>
    val returnTo = routes.User.login().absoluteURL()
    val logoutUrl = config.logoutUrl ? ("returnTo" -> returnTo) & ("client_id" -> config.clientId)
    Redirect(logoutUrl).withNewSession.discardingCookies(DiscardingCookie("idToken"), DiscardingCookie("accessToken"))
  }

}
