package controllers

import config.StravaConfig
import io.lemonlabs.uri.dsl._
import javax.inject.{Inject, Singleton}
import models.{Profile, StravaOauthToken}
import services.StravaOauthService

import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{BaseController, ControllerComponents, Request}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class Strava @Inject()(
  stravaOauthService: StravaOauthService,
  config: StravaConfig,
  ws: WSClient,
  cache: SyncCacheApi,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

  def authorize = Action { implicit request =>
    val url = config.authorizationUrl ?
      ("client_id" -> config.clientId) &
      ("response_type" -> "code") &
      ("redirect_uri" -> config.redirectUri) &
      ("scope" -> "read,activity:read")
    Redirect(url) // redirect user to authorize page on strava
  }

  def callback(maybeCode: Option[String]) = Action.async { implicit request =>
    getProfile(request) match {
      case Some(profile) =>
        maybeCode match {
          case Some(code) =>
            ws.url(config.oauthUrl).post(forTokenExchange(code)) flatMap { response =>
              val token = extractStravaOauthToken(response)
              val withStrava = profile.copy(stravaId = Some(token.athleteId), stravaToken = Some(token.accessToken))
              stravaOauthService.saveToken(withStrava, token) map { updated =>
                request.session.get("idToken") foreach (id => cache.set(id + "profile", updated))
                Redirect(routes.User.profile()).flashing("success" -> "Connected to Strava")
              } recover {
                case NonFatal(e) =>
                  Logger.error(s"Update failed for user: $withStrava with error $e")
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

  def tokenStatus = Action { implicit request =>
    Ok("Some shit about your access token is weird.")
  }

  private def getProfile(request: Request[Any]): Option[Profile] = {
    for {
      authId <- request.session.get("idToken")
      profile <- cache.get[Profile](authId + "profile")
    } yield {
      profile
    }
  }

  private def forTokenExchange(code: String): Map[String, Seq[String]] = {
    Map(
      "client_id" -> Seq(config.clientId),
      "client_secret" -> Seq(config.clientSecret),
      "code" -> Seq(code),
      "grant_type" -> Seq("authorization_code")
    )
  }

  private def extractStravaOauthToken(response: WSResponse) = {
    val json = Json.parse(response.body)
    val accessToken = (json \ "access_token").as[String]
    val expiry = (json \ "expires_at").as[Long]
    val refreshToken = (json \ "refresh_token").as[String]
    val athleteId = (json \ "athlete" \ "id").as[Int]

    StravaOauthToken(accessToken, Instant.ofEpochSecond(expiry), refreshToken, athleteId)
  }

}

