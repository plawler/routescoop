package controllers

import javax.inject.{Inject, Singleton}
import services.FitnessService

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

@Singleton
class Fitness @Inject()(
  fitnessService: FitnessService,
  val controllerComponents: ControllerComponents) extends BaseController with LazyLogging {

  def trainingLoad(userId: String, days: Int) = Action { implicit request =>
    val results = fitnessService.getTrainingLoad(userId, days)
    Ok(Json.toJson(results))
  }

  def rampRate(userId: String, days: Int) = Action { implicit request =>
    Ok(Json.toJson(fitnessService.getRampRate(userId, days)))
  }

}
