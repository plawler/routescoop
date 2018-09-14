package controllers

import java.util.UUID

import fixtures.{ProfileFixture, SettingsFixture}
import models.{NewSettings, Profile, SettingsResultSuccess}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{WordSpec, Matchers => ScalaTestMatchers}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.CacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SettingsService

import scala.concurrent.Future

class SettingsSpec extends WordSpec with ScalaTestMatchers with MockitoSugar with OneAppPerSuite {

  val mockCache = mock[CacheApi]
  val mockService = mock[SettingsService]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].toInstance(mockCache))
    .overrides(bind[SettingsService].toInstance(mockService))
    .build()

  "The Settings controller" should {

    "create new settings" in new SettingsTesting {
      val result = route(app, FakeRequest(GET, "/settings/new").withSession(session)).get
      status(result) shouldBe OK
      contentAsString(result) should include("<form action=\"/settings\" method=\"POST\" class=\"form-vertical\" role=\"form\">")
    }

    "save new settings" in new SettingsTesting {
      when(mockService.create(any[NewSettings])).thenReturn(Future.successful(createdResult))

      val result = route(app,
        FakeRequest(POST, "/settings")
          .withFormUrlEncodedBody("weight" -> "155", "ftp" -> "275", "hr" -> "195")
          .withSession(session)
      ).get

      status(result) shouldBe SEE_OTHER

      verify(mockService).create(any[NewSettings])
    }

    "list all settings for a user" in new SettingsTesting {
      when(mockService.list(profile.id)).thenReturn(Future.successful(listResult))

      val result = route(app, FakeRequest(GET, "/settings").withSession(session)).get
      status(result) shouldBe OK
      contentAsString(result) should include("<h1 class=\"h2\">Settings</h1>")
      contentAsString(result) should include(s"<td>${createdSettings.weight}</td>")
      contentAsString(result) should include(s"<td>${createdSettings.ftp}</td>")
      contentAsString(result) should include(s"<td>${createdSettings.maxHeartRate}</td>")
      contentAsString(result) should include(s"<td>${createdSettings.createdAt}</td>")
    }

  }

  trait SettingsTesting extends SettingsFixture {
    when(mockCache.get[Profile](profileKey)).thenReturn(Some(profileWithStrava))
  }

}
