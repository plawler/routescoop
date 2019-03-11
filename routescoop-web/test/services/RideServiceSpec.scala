package services

import config.AppConfig
import fixtures.RideFixture
import models.{RideSummaryResultSuccess, RideSyncResultStarted}

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server

import scala.concurrent.Await
import scala.concurrent.duration._

class RideServiceSpec extends WordSpec with Matchers {

  val config = new AppConfig("", "/api/v1")

  "The Ride Service" should {

    "sync rides with Strava" in new RideServiceTesting {
      Server.withRouter() {
        case POST(p"/api/v1/syncs") => Action {
          Accepted(Json.toJson(rideSync))
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new RideService(config, client)
          val result = Await.result(service.syncStrava(profileWithStrava.toUser), 1 second)
          result shouldEqual RideSyncResultStarted(rideSync)
        }
      }
    }

    "list ride summaries" in new RideServiceTesting {
      Server.withRouter() {
        case GET(p"/api/v1/users/${profileWithStrava.id}/activities") => Action { // do not include parameters in url
          Ok(rideSummaryPageJson)
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new RideService(config, client)
          val result = Await.result(service.listRideSummaries(profileWithStrava.toUser, 1), 5.seconds)
          result shouldEqual RideSummaryResultSuccess(summaries)
        }
      }
    }

  }

  trait RideServiceTesting extends RideFixture {

  }

}
