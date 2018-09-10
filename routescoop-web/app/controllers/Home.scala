package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class Home @Inject()(AuthenticatedAction: AuthenticatedAction, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = AuthenticatedAction { implicit request =>
    Ok(views.html.index())
  }

  def bootstrap = Action { implicit request =>
    Ok(views.html.test.bootstrap())
  }

}
