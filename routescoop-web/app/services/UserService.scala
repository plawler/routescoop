package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.{Profile, UserCreated, UserResult, UserResultError}
import modules.NonBlockingContext
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(ws: WSClient, config: AppConfig)(implicit @NonBlockingContext ec: ExecutionContext) {

  lazy val url = s"${config.baseApiUrl}/users"

  def createUser(profile: Profile): Future[UserResult] = {
    ws.url(url).post(Json.toJson(profile)) map { response =>
      response.status match {
        case Status.CREATED => UserCreated
        case _ => {
          val message = s"${response.status}: ${response.body}"
          UserResultError(message)
        }
      }
    }
  }

}
