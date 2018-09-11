package controllers

import fixtures.ProfileFixture
import models.Profile
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.CacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SettingsSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  val mockCache = mock[CacheApi]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].toInstance(mockCache))
    .build()

  "The Settings controller" should {

    "let a user create new settings" in new SettingsTesting {
      val result = route(app, FakeRequest(GET, "/settings/new").withSession(session)).get
      status(result) shouldBe OK
      contentAsString(result) should include("<form action=\"/settings\" method=\"POST\" class=\"form-vertical\" role=\"form\">")
    }
  }

  trait SettingsTesting extends ProfileFixture {
    when(mockCache.get[Profile](profileKey)).thenReturn(Some(profileWithStrava))
  }

}
