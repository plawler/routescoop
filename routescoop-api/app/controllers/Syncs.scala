package controllers

import javax.inject.Inject
import models.UserDataSyncRequest
import modules.NonBlockingContext
import services.{DataSyncService, UserService}

import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class Syncs @Inject()(userService: UserService, dataSyncService: DataSyncService)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller {

  def sync = Action.async(parse.json) { implicit request =>
    request.body.validate[UserDataSyncRequest].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      dsr => userService.getUser(dsr.userId) flatMap {
        case Some(user) => dataSyncService.sync(dsr) map (sync => Accepted(Json.toJson(sync)))
        case None => Future.successful(NotFound(s"User not found with id ${dsr.userId}"))
      }
    )
  }

}
