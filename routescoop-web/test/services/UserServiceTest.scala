package services

import config.AppConfig
import models.{Profile, UserCreated, UserResultError}
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
          val result = Await.result(service.createUser(profile), 1 second)
          result shouldEqual UserCreated
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
          val result = Await.result(service.createUser(profile.copy(userId = "")), 1 second)
          result shouldBe a [UserResultError]
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
