package services

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import config.AppConfig
import fixtures.{ProfileFixture, SettingsFixture}
import models.{NewSettings, Settings, SettingsResultSuccess}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Await
import scala.concurrent.duration._

class SettingsServiceSpec extends WordSpec with Matchers {

  val config = new AppConfig("", "/api/v1")

  "The Settings Service" should {

    "create settings" in new SettingsServiceTesting {
      Server.withRouter() {
        case POST(p"/api/v1/users/${profileWithStrava.id}/settings") => Action {
          Created(
            Json.obj(
              "id" -> createdSettings.id,
              "weight" -> createdSettings.weight,
              "ftp" -> createdSettings.ftp,
              "maxHeartRate" -> createdSettings.maxHeartRate,
              "createdAt" -> createdSettings.createdAt
            )
          )
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new SettingsService(client, config)
          val result = Await.result(service.create(newSettings), 1 second)
          result shouldEqual SettingsResultSuccess(createdSettings)
        }
      }
    }

  }

  trait SettingsServiceTesting extends SettingsFixture {

  }

}
