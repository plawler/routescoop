package controllers

import config.AppConfig
import javax.inject.Inject
import modules.NonBlockingContext
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

/**
  * Created by paullawler on 12/28/16.
  */
class Application @Inject()(appConfig: AppConfig, ws: WSClient)(implicit @NonBlockingContext ec: ExecutionContext)
  extends Controller {

  def ping = Action(implicit request => Ok("OK"))

  def health = Action.async { implicit request =>
    ws.url(s"${appConfig.apiHost}/ping").get() map { response =>
      if (response.body == "OK") {
        Ok("routescoop-api: ok") // todo: strava and auth0
      } else {
        Logger.error(response.body)
        BadGateway
      }
    }
  }

}
