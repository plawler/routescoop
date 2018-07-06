package services

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.Profile
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.Future

@Singleton
class UserService @Inject()(ws: WSClient, config: AppConfig) {

  lazy val url = s"${config.baseApiUrl}/users"

  def createUser(profile: Profile): Future[Unit] = {
    ws.url(url).post(Json.toJson(profile)) map { response =>
      response.status match {
        case Status.OK => Future.successful()
        case _ => {
          val message = s"${response.status}: ${response.body}"
          throw new IllegalStateException(message)
        }
      }
    }
  }

}
