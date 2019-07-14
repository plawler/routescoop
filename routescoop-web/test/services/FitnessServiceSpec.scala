package services

import config.AppConfig
import fixtures.ProfileFixture
import models.{CriticalPower, CriticalPowerResultSuccess, DailyFitness, Effort, FitnessTrendResultSuccess}

import org.scalatest.{Matchers, WordSpec}
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration._


class FitnessServiceSpec extends WordSpec with Matchers {

  val config = new AppConfig("", "/api/v1")

  "The FitnessService" should {

    "retrieve a user's fitness trend for a given number of days" in new FitnessServiceTesting {
      Server.withRouter() {
        case GET(p"/api/v1/users/${profileWithStrava.id}/fitness/90") => Action {
          Ok(Json.toJson(dailyFitnessList))
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new FitnessService(config, client)
          val result = Await.result(service.fitnessTrend(profileWithStrava.toUser, 90), 1 second)
          result shouldBe FitnessTrendResultSuccess(dailyFitnessList)
        }
      }
    }

    "retrieve the critical power for a user" in new FitnessServiceTesting {
      Server.withRouter() {
        case GET(p"/api/v1/users/${profileWithStrava.id}/cp/90") => Action {
          Ok(Json.toJson(criticalPower))
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new FitnessService(config, client)
          val result = Await.result(service.criticalPower(profileWithStrava.toUser, 90, cpDurationSamples), 1 second)
          result shouldBe CriticalPowerResultSuccess(criticalPower)
        }
      }
    }

  }

  trait FitnessServiceTesting extends ProfileFixture {
    val dailyFitnessList = List(
      DailyFitness(LocalDate.of(2018,11,26),0.0,0.0,0.0),
      DailyFitness(LocalDate.of(2018,11,27),1.7,10.0,-8.3),
      DailyFitness(LocalDate.of(2018,11,28),3.7,20.7,-17.0),
      DailyFitness(LocalDate.of(2018,11,29),4.6,23.7,-19.1),
      DailyFitness(LocalDate.of(2018,11,30),4.5,20.3,-15.8)
    )

    val criticalPower = CriticalPower(
      354.7,
      11520,
      Seq(
        Effort(60, 547),
        Effort(120, 451),
        Effort(180, 419),
        Effort(240, 403),
        Effort(300, 394),
        Effort(480, 379),
        Effort(600, 374),
        Effort(900, 368),
        Effort(1200, 365),
        Effort(2400, 360),
        Effort(3600, 358)
      )
    )

    val cpDurationSamples = Seq(180, 360, 720)
  }

}
