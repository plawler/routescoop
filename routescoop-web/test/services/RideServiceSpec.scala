package services

import java.time.Instant
import java.util.UUID

import config.AppConfig
import fixtures.ProfileFixture
import models.{RideSync, RideSyncResultStarted}
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
        case POST(p"/api/v1/users/${profileWithStrava.id}/syncs") => Action {
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

  }

  trait RideServiceTesting extends ProfileFixture {
    val userId = profileWithStrava.id
    val rideSync = RideSync(UUID.randomUUID().toString, userId, Instant.now)
  }

}
