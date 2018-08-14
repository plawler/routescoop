package controllers

import config.{AppConfig, AuthConfig}
import javax.inject.Inject
import modules.NonBlockingContext
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

/**
  * Created by paullawler on 12/28/16.
  */
class Application @Inject()(appConfig: AppConfig, authConfig: AuthConfig, ws: WSClient)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  val authTestUrl = s"${authConfig.domain}/test"
  val apiTestUrl = s"${appConfig.apiHost}/health"

  def ping = Action(implicit request => Ok("OK"))

  def health = Action.async { implicit request =>
    for {
      authOk <- ws.url(authTestUrl).get()
      apiOk <- ws.url(apiTestUrl).get()
    } yield {
      val healthMap = Map(
        authTestUrl -> (authOk.status, authOk.body),
        apiTestUrl -> (apiOk.status, apiOk.body)
      )

      if (healthMap.values exists (_._1 != OK)) {
        BadGateway(healthMap.toString())
      } else {
        Ok(healthMap.toString)
      }
    }
  }

}
