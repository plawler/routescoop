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

@Singleton
class UserService @Inject()(ws: WSClient, config: AppConfig)(implicit @NonBlockingContext ec: ExecutionContext) {

  lazy val url = s"${config.baseApiUrl}/users"

  def saveOrUpdate(profile: Profile): Future[UserResult] = {
    val user = profile.toUser
    get(user.id) flatMap {
      case UserResultSuccess(_) => update(user)
      case UserResultNotFound => create(user)
      case error: UserResultError => Future.successful(error)
    }
  }

  def create(user: User): Future[UserResult] = {
    val json = Json.toJson(user)
    ws.url(url).post(json) map { response =>
      response.status match {
        case Status.CREATED => UserResultSuccess(user)
        case _ => UserResultError(s"${response.status}: ${response.body}")
      }
    }
  }

  def update(user: User): Future[UserResult] = {
    ws.url(url).put(Json.toJson(user)) map { response =>
      response.status match {
        case Status.NO_CONTENT => UserResultSuccess(user)
        case _ => UserResultError(s"${response.status}: ${response.body}")
      }
    }
  }

  def get(id: String): Future[UserResult] = {
    val getUserUrl = s"$url/$id"
    ws.url(getUserUrl).get() map { response =>
      response.status match {
        case Status.OK =>
          response.json.validate[User] match {
            case success: JsSuccess[User] => UserResultSuccess(success.get)
            case error: JsError =>
              Logger.error(s"api response error: $error")
              UserResultError(s"user retrieved does not map to a profile")
          }
        case Status.NOT_FOUND => UserResultNotFound
        case _ => UserResultError(s"${response.status}: ${response.body}")
      }
    }
  }

}
