package controllers

import config.StravaConfig
import models.{Profile, StravaOauthToken, UserResultSuccess}
import services.{StravaOauthService, UserService}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.SyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{DefaultActionBuilder, PlayBodyParsers}
import play.api.routing.sird._
import play.api.test.{FakeRequest, StubControllerComponentsFactory, WsTestClient}
import play.core.server.Server

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StravaSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite with StubControllerComponentsFactory {

  val mockCache = mock[SyncCacheApi]
  val mockStravaOauthService = mock[StravaOauthService]

  implicit val as = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val bodyParser = PlayBodyParsers()
  val action = DefaultActionBuilder(bodyParser.anyContent)

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[SyncCacheApi].toInstance(mockCache))
    .overrides(bind[StravaOauthService].toInstance(mockStravaOauthService))
    .build()

  "The Strava controller" should {

    "callback with an authorization code" in new StravaTesting {
      Server.withRouter() {
        case POST(p"/oauth/token") => action {
          Ok(tokenExchangeJson)
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val controller = new Strava(mockStravaOauthService, config, client, mockCache, stubControllerComponents())
          val request = FakeRequest(play.api.http.HttpVerbs.GET, "/strava/callback").withSession("idToken" -> sessionId)
          val result = controller.callback(Some("abcdef123456")).apply(request)

          Await.result(result, 1 second)

          verify(mockStravaOauthService).saveToken(any(classOf[Profile]), any(classOf[StravaOauthToken]))
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

    val tokenExchangeJson = Json.parse(
      s"""
         |{
         |  "token_type": "Bearer",
         |  "expires_at": 1568775134,
         |  "expires_in": 21600,
         |  "refresh_token": "e5n567567",
         |  "access_token": "$stravaToken",
         |  "athlete": {
         |    "id": $stravaId
         |  }
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
    when(mockStravaOauthService.saveToken(any(classOf[Profile]), any(classOf[StravaOauthToken])))
      .thenReturn(Future.successful(stravaProfile))
  }

}
