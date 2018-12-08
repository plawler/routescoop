package controllers

import javax.inject.{Inject, Singleton}
import services.FitnessService

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

@Singleton
class Fitness @Inject()(fitnessService: FitnessService) extends Controller with LazyLogging {

  def list(userId: String, days: Int) = Action { implicit request =>
    val results = fitnessService.getDailyTrainingLoad(userId, days)
    Ok(Json.toJson(results))
  }

}
