package controllers

import javax.inject.{Inject, Singleton}
import models.{NewSettings, Profile, SettingsResultSuccess}
import modules.NonBlockingContext
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, Request}
import services.SettingsService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class Settings @Inject()(
  authenticated: AuthenticatedAction,
  settingsService: SettingsService,
  cache: CacheApi,
  val messagesApi: MessagesApi)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller with I18nSupport {

  def postUrl = routes.Settings.save()
  def listUrl = routes.Settings.list()

  def create = authenticated { implicit request =>
    Ok(views.html.settings.create(SettingsForm.form, routes.Settings.save()))
  }

  def save = authenticated.async { implicit request =>
    getProfile(request) match {
      case Some(profile) =>
        SettingsForm.form.bindFromRequest.fold(
          formWithErrors =>
            Future.successful(BadRequest(views.html.settings.create(formWithErrors, postUrl))),
          data => {
            settingsService.create(NewSettings(profile.id, data.weight, data.ftp, data.hr)) map {
              case srs: SettingsResultSuccess => Redirect(listUrl).flashing("success" -> "Settings saved")
              case _ => Redirect(listUrl).flashing("error" -> "Attempt to save settings failed")
            } recover {
              case NonFatal(e) =>
                Logger.error(s"failed to save settings $e")
                Redirect(listUrl).flashing("error" -> "Attempt to save settings failed to complete")
            }
          }
        )
      case None =>
        Logger.error("A profile wasn't found in cache for the user...logging out")
        Future.successful(Redirect(routes.Auth.logout()))
    }
  }

  def list = authenticated { implicit request =>
    Ok(request.flash.get("success").getOrElse("error"))
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

object SettingsForm {

  case class Data(weight: Int, ftp: Int, hr: Int)

  val form = Form(
    mapping(
      "weight" -> number(min = 0),
      "ftp" -> number(min = 0),
      "hr" -> number(min = 0)
    )(Data.apply)(Data.unapply)
  )

}
