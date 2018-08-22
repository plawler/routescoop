package controllers


import config.AuthConfig
import javax.inject.{Inject, Singleton}
import models.{UserCreated, UserResultError}
import modules.NonBlockingContext
import play.api.Logger
import play.api.cache.CacheApi
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Cookie}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthZero @Inject()(config: AuthConfig, ws: WSClient, cache: CacheApi, userService: UserService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def callback(maybeCode: Option[String] = None) = Action.async {
    (for {
      code <- maybeCode
    } yield {
      getToken(code) flatMap {
        case (idToken, accessToken) =>
          getAuthUser(accessToken) map { userJson =>
            cache.set(idToken + "profile", userJson.as[models.Profile])
            Redirect(routes.Home.index()).withCookies(
              Cookie("idToken", idToken, Some(36000)),
              Cookie("accessToken", accessToken, Some(36000))
            )
          }
      }
    }.recover {
      case ex: IllegalStateException => Unauthorized(ex.getMessage)
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def callback2(maybeCode: Option[String]) = Action.async { implicit request =>
    maybeCode match {
      case Some(code) =>
        for {
          (idToken, accessToken) <- getToken(code)
          json <- getAuthUser(accessToken)
          profile = json.as[models.Profile]
          result <- userService.createUser(profile)
        } yield {
          result match {
            case UserResultError(cause) =>
              Logger.error(cause)
              Redirect(routes.User.login())
            case UserCreated =>
              cache.set(idToken + "profile", profile)
              Redirect(routes.Home.index()).withCookies(
                Cookie("idToken", idToken, Some(36000)),
                Cookie("accessToken", accessToken, Some(36000))
              )
          }
        }
      case None => Future.successful(BadRequest("Authentication service did not return code"))
    }
  }

  private def getToken(code: String): Future[(String, String)] = {
    val json = Json.obj(
      "client_id" -> config.clientId,
      "client_secret" -> config.clientSecret,
      "redirect_uri" -> config.callbackUrl,
      "code" -> code,
      "grant_type" -> "authorization_code"
    )
    val tokenResponse = ws.url(config.tokenUrl).withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).post(json)
    tokenResponse.flatMap { response =>
      (for {
        idToken <- (response.json \ "id_token").asOpt[String]
        accessToken <- (response.json \ "access_token").asOpt[String]
      } yield {
        Future.successful((idToken, accessToken))
      }).getOrElse(Future.failed[(String, String)](new IllegalStateException("Tokens not sent")))
    }
  }

  private def getAuthUser(accessToken: String): Future[JsValue] = {
    val response = ws.url(config.fetchUserUrl).withQueryString("access_token" -> accessToken).get()
    response.flatMap(response => Future.successful(response.json))
  }

}
