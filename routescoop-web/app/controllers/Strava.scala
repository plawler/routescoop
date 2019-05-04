package controllers

import config.StravaConfig
import io.lemonlabs.uri.dsl._
import javax.inject.{Inject, Singleton}
import models.{Profile, UserResultSuccess}
import services.UserService

import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{BaseController, ControllerComponents, Request}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Strava @Inject()(
  userService: UserService,
  config: StravaConfig,
  ws: WSClient,
  cache: SyncCacheApi,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

  def authorize = Action { implicit request =>
    val url = config.authorizationUrl ?
      ("client_id" -> config.clientId) &
      ("response_type" -> "code") &
      ("redirect_uri" -> config.redirectUri)
    Redirect(url) // redirect user to authorize page on strava
  }

  def callback(maybeCode: Option[String]) = Action.async { implicit request =>
    getProfile(request) match {
      case Some(profile) =>
        maybeCode match {
          case Some(code) =>
            ws.url(config.oauthUrl).post(forTokenExchange(code)) flatMap { response =>
              val (token, id) = extractStravaBits(response)
              val withStrava = profile.copy(stravaId = Some(id), stravaToken = Some(token)).toUser
              userService.update(withStrava) map {
                case UserResultSuccess(user) =>
                  request.session.get("idToken") foreach (id => cache.set(id + "profile", user.toProfile))
                  Redirect(routes.User.profile()).flashing("success" -> "Connected to Strava")
                case _ =>
                  Logger.error(s"Update failed for user: $withStrava")
                  Redirect(routes.User.profile()).flashing("error" -> "Profile update failed")
              }
            }
          case None =>
            Logger.error("Strava did not callback with a code...redirecting with error")
            Future.successful(Redirect(routes.User.profile()).flashing("error" -> "Strava update failed"))
        }
      case None =>
        Logger.error("A profile wasn't found in cache for the user...logging out")
        Future.successful(Redirect(routes.Auth.logout()))
    }
  }

  private def getProfile(request: Request[Any]): Option[Profile] = {
    for {
      sessionId <- request.session.get("idToken")
      profile <- cache.get[Profile](sessionId + "profile")
    } yield {
      profile
    }
  }

  private def forTokenExchange(code: String): Map[String, Seq[String]] = {
    Map("client_id" -> Seq(config.clientId), "client_secret" -> Seq(config.clientSecret), "code" -> Seq(code))
  }

  private def extractStravaBits(response: WSResponse) = {
    val json = Json.parse(response.body)
    val token = (json \ "access_token").as[String]
    val athleteId = (json \ "athlete" \ "id").as[Int]
    (token, athleteId)
  }

}

