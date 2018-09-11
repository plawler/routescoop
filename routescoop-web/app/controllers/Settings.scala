package controllers

import javax.inject.Inject
import modules.NonBlockingContext
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

import scala.concurrent.{ExecutionContext, Future}

class Settings @Inject()(
  authenticated: AuthenticatedAction,
  cache: CacheApi,
  val messagesApi: MessagesApi)
  (implicit @NonBlockingContext ec: ExecutionContext) extends Controller with I18nSupport {

  def create = authenticated.async { implicit request =>
    Future.successful(Ok(views.html.settings.create(SettingsForm.form, routes.Settings.save())))
  }

  def save = authenticated.async { implicit request =>
    ???
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
