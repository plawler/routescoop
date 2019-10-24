package services

import config.{AppConfig, StravaConfig}
import javax.inject.{Inject, Singleton}
import models.{Profile, StravaOauthToken}

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

case class OauthTokenResponse(
  token_type: String,
  access_token: String,
  expires_at: Long,
  expires_in: Int,
  refresh_token: String,
  athlete_id: Int
)

object OauthTokenResponse {
  implicit val oauthTokenResponseFormat = Json.format[OauthTokenResponse]
}

@Singleton
class StravaOauthService @Inject()(ws: WSClient, stravaConfig: StravaConfig, appConfig: AppConfig)
  (implicit ec: ExecutionContext) {

  def refresh(profile: Profile): Future[Profile] = {
    profile.stravaOauthToken map { current =>
      if (current.isExpired) {
        Logger.info("Gonna be refreshing this Strava token as soon as Paul fixes his shit...")
        exchangeToken(current.refreshToken) flatMap {
          case Some(response) =>
            Logger.info(s"saving new token to ${profile.email} account")
            saveToken(profile, StravaOauthToken(response)).map(p => p)
          case None =>
            Logger.info("token not expired. no change to profile")
            Future.successful(profile)
        }
      } else {
        Future.successful(profile)
      }
    } getOrElse {
      Logger.warn(s"The profile $profile has no access token for Strava. How'd that happen?")
      Future.successful(profile)
    }
  }

  def saveToken(profile: Profile, token: StravaOauthToken): Future[Profile] = {
    val url = s"${appConfig.baseApiUrl}/users/${profile.id}/strava/tokens"
    ws.url(url).post(Json.toJson(token)) map { response =>
      response.status match {
        case Status.CREATED =>
          Logger.info(s"posting new strava oauth token to routescoop succeeded: $response")
          profile.copy(stravaOauthToken = Some(token))
        case _ =>
          Logger.error(s"posting new strava oauth token to routescoop failed: $response")
          profile
      }
    }
  }

  private def exchangeToken(refreshToken: String): Future[Option[OauthTokenResponse]] = {
    ws.url(stravaConfig.oauthUrl).post(forTokenExchange(refreshToken)) map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[OauthTokenResponse] match {
            case success: JsSuccess[OauthTokenResponse] =>
              Logger.info(s"Strava oauth response: $response")
              Some(success.get)
            case error: JsError =>
              Logger.error(s"Strava oauth api response error: $error")
              None
          }
        case _ =>
          Logger.error(s"Strava oauth response not okay: $response")
          None
      }
    }
  }

  private def forTokenExchange(refreshToken: String): Map[String, Seq[String]] = {
    Map(
      "client_id" -> Seq(stravaConfig.clientId),
      "client_secret" -> Seq(stravaConfig.clientSecret),
      "grant_type" -> Seq("refresh_token"),
      "refresh_token" -> Seq(refreshToken)
    )
  }

}
