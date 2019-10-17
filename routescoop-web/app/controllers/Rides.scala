package controllers

import javax.inject.{Inject, Singleton}
import models._
import services.{RideService, SettingsService}

import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Rides @Inject()(
  authenticated: AuthenticatedAction,
  stravaRefreshed: StravaTokenRefreshAction,
  rideService: RideService,
  settingsService: SettingsService,
  ws: WSClient,
  cache: SyncCacheApi,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  val syncRidesUrl = routes.Rides.sync()
  val createSettingsUrl = routes.Settings.create()

  def sync = authenticated.andThen(stravaRefreshed).async { implicit request =>
    FetchRidesForm.form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(routes.Rides.index()).flashing("error" -> "Fetch rides form invalid"))
      },
      data => {
        rideService.syncStrava(request.profile.toUser, data.fetchOlderRides) map {
          case RideSyncResultStarted(sync) =>
            Logger.info(s"activity sync started: $sync")
            Redirect(routes.Rides.index()).flashing("success" -> s"Sync started : ${sync.id}")
          case default =>
            Logger.info(s"activity sync error: $default")
            Redirect(routes.Rides.index()).flashing("error" -> s"Sync error: $default")
        }
      }
    )
  }

  def index(page: Int) = authenticated.async { implicit request =>
    val profile = request.profile
    hasSettings(profile.id) flatMap { hasSettings =>
      val url = if (hasSettings) syncRidesUrl else createSettingsUrl
      rideService.listRideSummaries(profile.toUser, page) map {
        case RideSummaryResultSuccess(summaries) =>
          Ok(views.html.rides.index(hasSettings, summaries, url, FetchRidesForm.form))
        case RideSummaryResultError(message) =>
          Logger.error(message)
          Ok(
            views.html.rides.index(hasSettings, Seq(), url, FetchRidesForm.form)
          ).flashing("error" -> "Failed to retrieve your ride list")
      }
    }
  }

  private def hasSettings(userId: String): Future[Boolean] = {
    settingsService.list(userId) map {
      case SettingsResultSuccess(settings) => settings.nonEmpty
      case _ => false
    }
  }

}

object FetchRidesForm {

  case class Data(fetchOlderRides: Boolean)

  val form = Form(
    mapping(
      "fetchOlderRides" -> boolean
    )(Data.apply)(Data.unapply)
  )

}
