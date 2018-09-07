package controllers

import java.util.UUID

import config.StravaConfig
import models.{Profile, UserResultSuccess}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.CacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.routing.sird._
import play.api.test.{FakeRequest, WsTestClient}
import play.core.server.Server
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class StravaSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  val mockCache = mock[CacheApi]
  val mockUserService = mock[UserService]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].toInstance(mockCache))
    .overrides(bind[UserService].toInstance(mockUserService))
    .build()

  "The Strava controller" should {

    "callback with an authorization code" in new StravaTesting {
      Server.withRouter() {
        case POST(p"/oauth/token") => Action {
          Ok(tokenExchangeJson)
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val controller = new Strava(mockUserService, config, client, mockCache)
          val request = FakeRequest(play.api.http.HttpVerbs.GET, "/strava/callback").withSession("idToken" -> sessionId)
          val result = controller.callback(Some("abcdef123456")).apply(request)

          Await.result(result, 1 second)

          verify(mockUserService).update(stravaUser)
        }
      }
    }

  }

  trait StravaTesting {
    val sessionId = UUID.randomUUID().toString
    val profileKey = sessionId + "profile"

    val id = "e6ef344c-3220-4522-9210-f76c1a8e0b49"
    val name = "Bob"
    val email = "bob@strava.com"
    val pic = ""
    val profile = Profile(id, name, email, pic)

    val stravaId = 123456
    val stravaToken = "aaaabbbb"
    val stravaProfile = profile.copy(stravaId = Some(stravaId), stravaToken = Some(stravaToken))
    val stravaUser = stravaProfile.toUser

    val tokenExchangeJson = Json.parse(
      s"""
         |{
         | "access_token": "$stravaToken",
         | "athlete": {
         |   "id": $stravaId
         | }
         |}
        """.stripMargin
    )

    val config = StravaConfig(
      "clientId",
      "clientSecret",
      "/oauth/authorize",
      "/strava/callback",
      "/oauth/token"
    )

    when(mockCache.get[Profile](profileKey)).thenReturn(Some(profile))
    when(mockUserService.update(stravaUser)).thenReturn(Future.successful(UserResultSuccess(stravaUser)))
  }

}
