package controllers

import javax.inject.Singleton

import com.google.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class Dashboard @Inject()(val controllerComponents: ControllerComponents)
  (implicit ec: ExecutionContext) extends BaseController {

}
