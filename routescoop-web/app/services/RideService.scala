package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import modules.NonBlockingContext
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RideService @Inject()(config: AppConfig, ws: WSClient)(implicit @NonBlockingContext ec: ExecutionContext) {

  val url = s"${config.baseApiUrl}/users"

  def syncStrava(user: User): Future[RideSyncResult] = {
    val syncUrl = s"$url/${user.id}/syncs"
    ws.url(syncUrl).post(Results.EmptyContent()) map { response =>
      response.status match {
        case Status.ACCEPTED =>
          response.json.validate[RideSync] match {
            case success: JsSuccess[RideSync] => RideSyncResultStarted(success.get)
            case error: JsError =>
              Logger.error(s"api response error: $error")
              RideSyncResultError("Attempt to sync rides failed")
          }
        case Status.NOT_FOUND => RideSyncResultError(s"User ${user.id} not found")
      }
    }
  }

}
