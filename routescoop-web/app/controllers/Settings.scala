package controllers

import javax.inject.{Inject, Singleton}
import models.{NewSettings, Profile, SettingsResultError, SettingsResultSuccess}
import services.SettingsService

import play.api.Logger
import play.api.cache.{CacheApi, SyncCacheApi}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class Settings @Inject()(
  authenticated: AuthenticatedAction,
  settingsService: SettingsService,
  cache: SyncCacheApi,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def postUrl = routes.Settings.save()
  def listUrl = routes.Settings.list()

  def create = authenticated { implicit request =>
    Ok(views.html.settings.create(SettingsForm.form, routes.Settings.save()))
  }

  def save = authenticated.async { implicit request =>
    SettingsForm.form.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest(views.html.settings.create(formWithErrors, postUrl))),
      data => {
        settingsService.create(toNewSettings(request.profile, data)) map {
          case srs: SettingsResultSuccess => Redirect(listUrl).flashing("success" -> "Settings saved")
          case _ => Redirect(listUrl).flashing("error" -> "Attempt to save settings failed")
        } recover {
          case NonFatal(e) =>
            Logger.error(s"failed to save settings $e")
            Redirect(listUrl).flashing("error" -> "Attempt to save settings failed to complete")
        }
      }
    )
  }

  def list = authenticated.async { implicit request =>
    settingsService.list(request.profile.id) map {
      case SettingsResultSuccess(settings) => Ok(views.html.settings.list(settings))
      case SettingsResultError(message) =>
        Logger.error(s"Failed to retrieve settings: $message")
        Ok(views.html.settings.list(Seq())).flashing("error" -> "Failed to retrieve settings")
    }
  }

  private def toNewSettings(profile: Profile, data: SettingsForm.Data) = {
    val createdOn = data.createdOn map (d => LocalDate.parse(d, dtf)) getOrElse LocalDate.now()
    NewSettings(profile.id, data.weight, data.ftp, data.hr, createdOn)
  }

}

object SettingsForm {

  case class Data(weight: Int, ftp: Int, hr: Int, createdOn: Option[String] = None)

  val form = Form(
    mapping(
      "weight" -> number(min = 0),
      "ftp" -> number(min = 0),
      "hr" -> number(min = 0),
      "createdOn" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

}
