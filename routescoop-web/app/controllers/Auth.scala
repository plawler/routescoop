package controllers


import java.util.UUID

import config.AuthConfig
import io.lemonlabs.uri.dsl._
import javax.inject.{Inject, Singleton}
import models.{Profile, UserResultError, UserResultNotFound, UserResultSuccess}
import modules.NonBlockingContext
import play.api.Logger
import play.api.cache.CacheApi
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller, Cookie, DiscardingCookie}
import services.UserService
import util.RandomUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal


@Singleton
class Auth @Inject()(config: AuthConfig, ws: WSClient, cache: CacheApi, userService: UserService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def loginUrl(state: String) = config.domain / "authorize" ?
    ("client_id" -> config.clientId) &
    ("redirect_uri" -> config.callbackUrl) &
    ("response_type" -> "code") &
    ("audience" -> config.getAudience) &
    ("state" -> state)


  def login = Action { implicit request =>
    val state = RandomUtil.alphanumeric()
    cache.set("state", state)
    Redirect(loginUrl(state))
  }

  def logout = Action { implicit request =>
    val returnTo = routes.Auth.login().absoluteURL()
    val logoutUrl = config.logoutUrl ? ("returnTo" -> returnTo) & ("client_id" -> config.clientId)
    Redirect(logoutUrl).withNewSession
  }

  def callback(maybeCode: Option[String]) = Action.async { implicit request =>
    maybeCode match {
      case Some(code) =>
        for {
          (idToken, accessToken) <- getTokens(code)
          profile <- getAuthProfile(accessToken)
          result <- userService.saveOrUpdate(profile)
        } yield {
          result match {
            case UserResultSuccess(user) =>
              cache.set(idToken + "profile", user.toProfile)
              Redirect(routes.Home.index()).withSession("idToken" -> idToken, "accessToken" -> accessToken)
            case UserResultError(message) =>
              Logger.error(message)
              Redirect(routes.Auth.login()) // todo: flash message
            case _ =>
              Logger.error(s"unknown shit happened $result")
              Redirect(routes.Auth.login())
          }
        }
      case None => Future.successful(BadRequest("Authentication service did not return code"))
    }
  }

  private def getTokens(code: String): Future[(String, String)] = {
    val json = Json.obj(
      "client_id" -> config.clientId,
      "client_secret" -> config.clientSecret,
      "redirect_uri" -> config.callbackUrl,
      "code" -> code,
      "grant_type" -> "authorization_code",
      "audience" -> config.getAudience
    )

    val tokenResponse = ws.url(config.tokenUrl).withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).post(json)

    tokenResponse map { response =>
      val idToken = (response.json \ "id_token").asOpt[String] getOrElse UUID.randomUUID().toString
      val accessToken = (response.json \ "access_token").as[String]
      (idToken, accessToken)
    } recover {
      case NonFatal(e) => throw new IllegalStateException(s"Token response failed: $e")
    }
  }

  private def getAuthProfile(accessToken: String): Future[Profile] = {
    val response = ws.url(config.fetchUserUrl).withQueryString("access_token" -> accessToken).get()
    response flatMap { response =>
      Future.successful(response.json.as[models.Profile])
    }
  }

}
