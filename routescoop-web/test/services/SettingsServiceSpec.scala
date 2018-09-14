package services

import config.AppConfig
import fixtures.SettingsFixture
import models.{SettingsResultError, SettingsResultSuccess}
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

class SettingsServiceSpec extends WordSpec with Matchers {

  val config = new AppConfig("", "/api/v1")

  "The Settings Service" should {

    "create settings" in new SettingsServiceTesting {
      Server.withRouter() {
        case POST(p"/api/v1/users/${profile.id}/settings") => Action {
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
          result shouldEqual createdResult
        }
      }
    }

    "list settings" in new SettingsServiceTesting {
      Server.withRouter() {
        case GET(p"/api/v1/users/${profile.id}/settings") => Action {
          Ok(Json.toJson(allSettings))
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new SettingsService(client, config)
          val result = Await.result(service.list(profile.id), 1 second)
          result shouldEqual listResult
        }
      }
    }

  }

  trait SettingsServiceTesting extends SettingsFixture {

  }

}
