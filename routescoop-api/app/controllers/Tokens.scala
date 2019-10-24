package controllers

import javax.inject.Inject
import models.StravaOauthToken
import modules.NonBlockingContext
import repositories.StravaOauthTokenStore

import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BaseController, ControllerComponents, Results}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class Tokens @Inject()(
  store: StravaOauthTokenStore,
  val controllerComponents: ControllerComponents
)(implicit @NonBlockingContext ec: ExecutionContext) extends BaseController {

  private case class TokenPostRequest(accessToken: String, expiresAt: Instant, refreshToken: String, athleteId: Int)
  private implicit val reads = Json.reads[TokenPostRequest]

  def createStravaToken(userId: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[TokenPostRequest].fold(
      errors => Future.successful(Results.BadRequest(JsError.toJson(errors))),
      token => {
        val sot = StravaOauthToken(userId, token.accessToken, token.expiresAt, token.refreshToken, token.athleteId)
        store.insert(sot)
        Future.successful(Results.Created)
      }
    )
  }

}
