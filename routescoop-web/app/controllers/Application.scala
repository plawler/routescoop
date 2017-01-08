package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}

/**
  * Created by paullawler on 12/28/16.
  */
class Application @Inject()() extends Controller {

  def ping = Action(implicit request => Ok("OK"))

}
