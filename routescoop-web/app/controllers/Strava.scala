package controllers

import io.lemonlabs.uri.dsl._
import config.StravaConfig
import javax.inject.{Inject, Singleton}
import models.{Profile, UserResultError, UserResultSuccess}
import modules.NonBlockingContext
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Strava @Inject()(userService: UserService, config: StravaConfig, ws: WSClient, cache: CacheApi)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def authorize = Action { implicit request =>
    val url = config.authorizationUrl ?
      ("client_id" -> config.clientId) &
      ("response_type" -> "code") &
      ("redirect_uri" -> config.redirectUri)
    Redirect(url) // redirect user to authorize page on strava
  }

  def callback(code: Option[String]) = Action.async { implicit request =>
    code.map { c =>
      // each user must authorize the app
      // once token is granted, it remains unless app is unauthorized
      ws.url(config.oauthUrl).post(config.forTokenExchange(c)) map { response =>
        val (token, stravaId) = extractStravaBits(response)
        request.session.get("idToken").foreach(idToken => updateUser(idToken, token, stravaId))
        Redirect(routes.User.profile())
      }
    }.getOrElse(Future.successful(Ok("failed to get code back from strava")))
  }

  def synchData(userId: String) = Action { implicit request =>
    Ok("gimme an athlete to synch")
  }

  private def updateUser[A](idToken: String, stravaToken: String, stravaId: Int): Unit = {
    val key = idToken + "profile"
    val maybeProfile = cache.get[models.Profile](key)

    maybeProfile map { profile =>
      val withStrava = profile.copy(stravaToken = Some(stravaToken), stravaId = Some(stravaId))
      userService.update(withStrava.toUser) map {
        case success: UserResultSuccess => cache.set(key, withStrava)
        case UserResultError(message) => throw new IllegalStateException(message)
      }
    } getOrElse(throw new IllegalStateException(s"no profile found in cache for id $idToken"))

  }

  private def extractStravaBits(response: WSResponse) = {
    val json = Json.parse(response.body)
    val token = (json \ "access_token").as[String]
    val athleteId = (json \ "athlete" \ "id").as[Int]
    (token, athleteId)
  }

}
