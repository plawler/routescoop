package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import modules.NonBlockingContext
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SettingsService @Inject()(ws: WSClient, config: AppConfig)(implicit ec: ExecutionContext) {

  def create(settings: NewSettings): Future[SettingsResult] = {
    val postUrl = s"${config.baseApiUrl}/users/${settings.userId}/settings"
    ws.url(postUrl).post(Json.toJson(settings)) map { response =>
      response.status match {
        case Status.CREATED =>
          response.json.validate[Settings] match {
            case success: JsSuccess[Settings] => SettingsResultSuccess(Seq(success.get))
            case error: JsError =>
              Logger.error(s"api response error: $error")
              SettingsResultError(s"api response error: failed to match response")
          }
        case _ =>
          Logger.error(s"api response error ${response.status}: ${response.body}")
          SettingsResultError(s"api response error: ${response.status}")
      }
    } recover {
      case NonFatal(e) => throw new IllegalStateException(e)
    }
  }

  def list(userId: String): Future[SettingsResult] = {
    val listUrl = s"${config.baseApiUrl}/users/${userId}/settings"
    ws.url(listUrl).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[Seq[Settings]] match {
            case success: JsSuccess[Seq[Settings]] =>
              SettingsResultSuccess(success.get)
            case error: JsError =>
              Logger.error(s"api response error: $error")
              SettingsResultError(s"api response error: failed to match response")
          }
        case _ =>
          Logger.error(s"api response error ${response.status}: ${response.body}")
          SettingsResultError(s"api response error: ${response.status}")
      }
    } recover {
      case NonFatal(e) => throw new IllegalStateException(e)
    }
  }

}
