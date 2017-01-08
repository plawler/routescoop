package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import models.DataSyncRequest
import modules.NonBlockingContext
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.{ExecutionContext, Future}


class User @Inject()(userService: UserService, actorSystem: ActorSystem)
                    (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def create = Action.async(parse.json) { implicit request =>
    request.body.validate[models.User].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      user => userService.createUser(user) map (_ => Ok)
    )
  }

  def get(id: String) = Action.async { implicit request =>
    userService.getUser(id).map { user =>
      if (user.isEmpty) NotFound
      else Ok(Json.toJson(user))
    }
  }

  def sync(userId: String) = Action.async { implicit request =>
    userService.getUser(userId) map {
      case Some(user) =>
        val dsr = DataSyncRequest(user)
        actorSystem.eventStream.publish(dsr)
        Ok(Json.toJson(dsr))
      case None => NotFound(s"User not found with id $userId")
    }
  }

}
