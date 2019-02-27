package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.{DailyFitness, FitnessTrendResult, FitnessTrendResultError, FitnessTrendResultSuccess, User}
import modules.NonBlockingContext

import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FitnessService @Inject()(config: AppConfig, ws: WSClient)(implicit @NonBlockingContext ec: ExecutionContext) {

  val url = s"${config.baseApiUrl}/users"

  def fitnessTrend(user: User, days: Int): Future[FitnessTrendResult] = {
    val fitnessUrl = s"$url/${user.id}/fitness/$days"
    ws.url(fitnessUrl).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[Seq[DailyFitness]] match {
            case success: JsSuccess[Seq[DailyFitness]] => FitnessTrendResultSuccess(success.get)
            case error: JsError => FitnessTrendResultError(s"api response error: $error")
          }
        case _ => FitnessTrendResultError(s"fetching fitness trend failed with status ${response.status}")
      }
    }
  }

}
