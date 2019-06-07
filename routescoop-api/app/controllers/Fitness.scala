package controllers

import javax.inject.{Inject, Singleton}
import models.Simulation
import services.FitnessService

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.Future

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

  def criticalPower(userId: String, days: Int, intervals: Seq[Int]) = Action { implicit request =>
    Ok(Json.toJson(fitnessService.getCriticalPower(userId, days, intervals)))
  }

  def simulation = Action.async(parse.json) { implicit request =>
    request.body.validate[Simulation].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      simulation => {
        val cp = fitnessService.simulateCriticalPower(simulation)
        Future.successful(Created(Json.toJson(cp)))
      }
    )
  }

}
