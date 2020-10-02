package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import modules.NonBlockingContext

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Results

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RideService @Inject()(config: AppConfig, ws: WSClient)(implicit ec: ExecutionContext) {

  val url = s"${config.baseApiUrl}"

  def syncStrava(user: User, fetchOlderRides: Boolean = false): Future[RideSyncResult] = {
    val syncUrl = s"$url/syncs"
    ws.url(syncUrl).post(Json.obj("userId" -> user.id, "fetchOlderRides" -> fetchOlderRides)) map { response =>
      response.status match {
        case Status.ACCEPTED =>
          response.json.validate[RideSync] match {
            case success: JsSuccess[RideSync] => RideSyncResultStarted(success.get)
            case error: JsError =>
              Logger.error(s"api response error: $error")
              RideSyncResultError("Attempt to sync rides failed")
          }
        case Status.NOT_FOUND => RideSyncResultError(s"User ${user.id} not found")
        case Status.BAD_REQUEST => RideSyncResultError(s"Bad request: $response")
      }
    }
  }

  def listRideSummaries(user: User, page: Int): Future[RideSummaryResult] = {
    val summaryUrl = s"$url/users/${user.id}/activities?page=$page"
    ws.url(summaryUrl).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[Seq[RideSummary]] match {
            case success: JsSuccess[Seq[RideSummary]] => RideSummaryResultSuccess(success.get)
            case error: JsError =>
              Logger.error(s"api response error: $error")
              RideSummaryResultError("Fetching ride summaries failed")
          }
        case _ => RideSummaryResultError(s"Fetching ride summaries failed with status ${response.status}")
      }
    }
  }

  def getRideDetails(rideId: String): Future[RideDetailsResult] = ???

}
