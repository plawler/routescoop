package controllers

import javax.inject.Inject
import models.{CreateUserSettings, User, UserSettings}
import modules.NonBlockingContext
import services.UserService

import akka.actor.ActorSystem
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}


class Users @Inject()(userService: UserService, actorSystem: ActorSystem)
                     (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

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
    userService.getUser(id).map { user =>
      if (user.isEmpty) NotFound
      else Ok(Json.toJson(user))
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

}
