package services

import config.AppConfig
import models.{Profile, UserResultError, UserResultSuccess}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.sird._
import play.api.test._
import play.core.server.Server

import scala.concurrent.Await
import scala.concurrent.duration._

class UserServiceTest extends WordSpec with Matchers {

  val config = new AppConfig("", "/api/v1")

  "User Service" should {

    "post new users to api" in new ProfileFixture {
      Server.withRouter() {
        case POST(p"/api/v1/users") => Action {
          Created(Json.obj("id" -> id, "name" -> name, "email" -> email))
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new UserService(client, config)
          val result = Await.result(service.saveOrUpdate(profile), 1 second)
          result shouldEqual UserResultSuccess("user created", profile)
        }
      }
    }

    "post invalid user payload" in new ProfileFixture {
      Server.withRouter() {
        case POST(p"/api/v1/users") => Action {
          BadRequest
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new UserService(client, config)
          val result = Await.result(service.saveOrUpdate(profile.copy(id = "")), 1 second)
          result shouldBe a [UserResultError]
        }
      }
    }

    "update user with new profile attributes" in new ProfileFixture {
      Server.withRouter() {
        case PUT(p"/api/v1/users") => Action {
          NoContent
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new UserService(client, config)
          val updated = profile.copy(stravaId = Some(1234567890), stravaToken = Some("theStravaToken"))
          val result = Await.result(service.update(updated), 1 second)
          result shouldEqual UserResultSuccess("user updated", updated)
        }
      }
    }

  }

  trait ProfileFixture {
    val id = "e6ef344c-3220-4522-9210-f76c1a8e0b49"
    val name = "Bob"
    val email = "bob@strava.com"
    val pic = ""
    val profile = Profile(id, name, email, pic)
  }

}
