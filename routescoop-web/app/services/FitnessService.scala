package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._

import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FitnessService @Inject()(config: AppConfig, ws: WSClient)(implicit ec: ExecutionContext) {

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

  def criticalPower(user: User, days: Int, durations: Seq[Int]): Future[CriticalPowerResult] = {
    val durationParams = durations map (d => ("intervals", d.toString))
    val cpUrl = s"$url/${user.id}/cp/$days"
    ws.url(cpUrl).withQueryStringParameters(durationParams: _*).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[CriticalPower] match {
            case success: JsSuccess[CriticalPower] => CriticalPowerResultSuccess(success.get)
            case error: JsError => CriticalPowerResultError(s"api response error: $error")
          }
        case _ => CriticalPowerResultError(s"fetching critical power failed with status ${response.status}")
      }
    }
  }

  def meanMaximalPower(user: User, days: Int): Future[MeanMaxPowerResult] = {
    val mmpUrl = s"$url/${user.id}/mmp?days=$days"
    ws.url(mmpUrl).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[Seq[Effort]] match {
            case success: JsSuccess[Seq[Effort]] => MeanMaxPowerResultSuccess(MeanMaxPower(success.get))
            case error: JsError => MeanMaxPowerResultError(s"api response error: ${error.errors}")
          }
        case _ => MeanMaxPowerResultError(s"fetching mean max power failed with status ${response.status}")
      }
    }
  }

  def powerProfile(user: User, days: Int, durations: Seq[Int]): Future[PowerProfileResult] = {
    val gotCp = criticalPower(user, days, durations)
    val gotMmp = meanMaximalPower(user, days)
    for {
      cpr <- gotCp
      mmpr <- gotMmp
    } yield {
      (cpr, mmpr) match {
        case (CriticalPowerResultSuccess(cp), MeanMaxPowerResultSuccess(mmp)) =>
          PowerProfileResultSuccess(PowerProfile(cp, mmp))
        case _ =>
          PowerProfileResultError(s"power profile api requests failed: $cpr $mmpr")
      }
    }
  }

}
