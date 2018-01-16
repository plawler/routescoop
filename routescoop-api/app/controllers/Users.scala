package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import models.{CreateUserSettings, UserSettings, User}
import modules.NonBlockingContext
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}


class Users @Inject()(userService: UserService, actorSystem: ActorSystem)
                     (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      user => userService.createUser(user) map (_ => Ok(Json.toJson(user)))
    )
  }

  def get(id: String) = Action.async { implicit request =>
    userService.getUser(id).map { user =>
      if (user.isEmpty) NotFound
      else Ok(Json.toJson(user))
    }
  }

  def sync(userId: String) = Action.async { implicit request =>
    userService.getUser(userId) flatMap {
      case Some(user) => userService.startDataSync(user) map (sync => Ok(Json.toJson(sync)))
      case None => Future.successful(NotFound(s"User not found with id $userId"))
    }
  }

  def createSettings(id: String) = Action.async(parse.json) { implicit request =>
    request.body.validate[CreateUserSettings].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      createSettings => {
        val userSettings = UserSettings.of(createSettings)
        userService.createSettings(userSettings) map (_ => Ok(Json.toJson(userSettings)))
      }
    )
  }

}
