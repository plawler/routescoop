package services

import config.AppConfig
import fixtures.ProfileFixture
import models.{UserResultError, UserResultSuccess}
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

class UserServiceSpec extends WordSpec with Matchers {

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
          result shouldEqual UserResultSuccess(profile.toUser)
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

    "update a user" in new ProfileFixture {
      Server.withRouter() {
        case PUT(p"/api/v1/users") => Action {
          NoContent
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new UserService(client, config)
          val updated = profileWithStrava.toUser
          val result = Await.result(service.update(updated), 1 second)
          result shouldEqual UserResultSuccess(updated)
        }
      }
    }

    "update an existing user with new profile attributes" in new ProfileFixture {
      Server.withRouter() {
        case GET(p"/api/v1/users/${profile.id}") => Action {
          Ok(Json.toJson(profileWithStrava))
        }
        case PUT(p"/api/v1/users") => Action {
          NoContent
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val service = new UserService(client, config)
          val updatedProfile = profileWithStrava.copy(email = "bob@auth0.com")
          val result = Await.result(service.saveOrUpdate(updatedProfile), 1 second)
          result shouldEqual UserResultSuccess(updatedProfile.toUser)
        }
      }
    }

  }

}
