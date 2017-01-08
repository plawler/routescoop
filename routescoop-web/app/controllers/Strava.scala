package controllers

import akka.actor.ActorSystem
import config.StravaAccessConfig
import models.{StravaDataSyncRequest, Profile}
import modules.NonBlockingContext

import com.netaporter.uri.dsl._
import play.api.Logger
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Strava @Inject() (config: StravaAccessConfig, ws: WSClient, cache: CacheApi, actorSystem: ActorSystem)
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
      ws.url(config.oauthUrl).post(config.forTokenExchange(c)).map { response =>
        val (token, stravaId) = extractStravaBits(response)
        request.cookies.get("idToken").foreach(cookie => updateProfile(cookie.value, token, stravaId))
        Redirect(routes.User.profile())
      }
    }.getOrElse(Future.successful(Ok("failed to get code back from strava")))
  }

  def synchData(userId: String) = Action { implicit request =>
    actorSystem.eventStream.publish(StravaDataSyncRequest("userToken", userId.toInt))
    Ok("gimme an athlete to synch")
  }

  private def updateProfile[A](idToken: String, stravaToken: String, stravaId: Int): Unit = {
    val key = idToken + "profile"
    val maybeProfile = cache.get[Profile](key)
    maybeProfile.foreach { profile =>
      val stravaProfile = profile.copy(stravaToken = Some(stravaToken), stravaId = Some(stravaId))
      cache.set(key, stravaProfile)
    }
  }

  private def extractStravaBits(response: WSResponse) = {
    val json = Json.parse(response.body)
    val token = (json \ "access_token").as[String]
    val athleteId = (json \ "athlete" \ "id").as[Int]
    (token, athleteId)
  }

}
