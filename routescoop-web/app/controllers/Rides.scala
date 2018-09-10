package controllers

import javax.inject.Inject
import models.{Profile, RideSyncResultStarted}
import modules.NonBlockingContext
import play.api.Logger
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.mvc.{Controller, Request}
import services.RideService

import scala.concurrent.{ExecutionContext, Future}

class Rides @Inject()(
  authenticated: AuthenticatedAction,
  rideService: RideService,
  ws: WSClient,
  cache: CacheApi,
  val messagesApi: MessagesApi)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller with I18nSupport {

  def sync = authenticated.async { implicit request =>
    getProfile(request) match {
      case Some(profile) =>
        rideService.syncStrava(profile.toUser) map {
          case RideSyncResultStarted(sync) => Ok(s"Sync started : $sync")
          case default => Ok(s"Sync error: $default")
        }
      case None =>
        Logger.error("A profile wasn't found in cache for the user...logging out")
        Future.successful(Redirect(routes.Auth.logout()))
    }
  }

  def index = authenticated  { implicit request =>
    Ok(views.html.rides.index())
  }

  private def getProfile(request: Request[Any]): Option[Profile] = {
    for {
      sessionId <- request.session.get("idToken")
      profile <- cache.get[Profile](sessionId + "profile")
    } yield {
      profile
    }
  }

}
