package controllers

import javax.inject.Inject
import models.{CreateUserSettings, StravaOauthToken, User, UserSettings}
import modules.NonBlockingContext
import repositories.StravaOauthTokenStore
import services.UserService

import play.api.Logger
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}


class Users @Inject()(
  userService: UserService,
  tokenStore: StravaOauthTokenStore,
  val controllerComponents: ControllerComponents
) (implicit @NonBlockingContext ec: ExecutionContext) extends BaseController {

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      user => userService.createUser(user) map (_ => Created(Json.toJson(user)))
    )
  }

  def update = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      user => userService.updateUser(user) map (_ => NoContent)
    )
  }

  def get(id: String) = Action.async { implicit request =>
    userService.getUser(id).map { maybeUser =>
      maybeUser map { user =>
        val maybeToken = tokenStore.findByUserId(user.id).headOption
        val theJson = makeUserJson(user, maybeToken)
        Logger.info(s"outgoing user json is: ${Json.prettyPrint(theJson)}")
        Ok(theJson)
      } getOrElse NotFound
    }
  }

  def createSettings(userId: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateUserSettings].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      createSettings => {
        val userSettings = UserSettings.of(createSettings)
        userService.createSettings(userSettings) map (_ => Created(Json.toJson(userSettings)))
      }
    )
  }

  def getAllSettings(userId: String) = Action.async { implicit request =>
    userService.getAllSettings(userId) map { settingsList =>
      Ok(Json.toJson(settingsList))
    }
  }

  private def makeUserJson(user: User, maybeToken: Option[StravaOauthToken]) = {
    val userJsObject = Json.toJsObject(user).fieldSet map ( x => Tuple2(x._1, Json.toJsFieldJsValueWrapper(x._2)))
    val fields = maybeToken match {
      case Some(token) => userJsObject.toSeq :+ ("stravaOauthToken" -> makeTokenJson(token))
      case None => userJsObject.toSeq
    }
    Json.obj(fields:_*)
  }

  private def makeTokenJson(token: StravaOauthToken) = {
    Json.toJsFieldJsValueWrapper(Json.toJsObject(token).-("userId"))
  }

}
