package controllers

import config.{AppConfig, AuthConfig}
import javax.inject.Inject

import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Created by paullawler on 12/28/16.
  */
class Application @Inject()(
  appConfig: AppConfig,
  authConfig: AuthConfig,
  ws: WSClient,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

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
