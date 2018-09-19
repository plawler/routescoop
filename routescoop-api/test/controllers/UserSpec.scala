package controllers

import fixtures.UserFixture
import models.CreateUserSettings
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import repositories.UserStore
import services.UserService

/**
  * Created by paullawler on 1/22/17.
  */
class UserSpec extends WordSpec
  with Matchers
  with MockitoSugar
  with OneAppPerSuite
  with BeforeAndAfterAll
  with UserSpecFixture {

  override def beforeAll() = {
    app.injector.instanceOf(classOf[UserStore]).destroy()
  }

  val mockService = mock[UserService]

  "The User API" should {

    "create a user" in {
      val result = route(app, FakeRequest(POST, usersApi, jsonHeader, userJson)).get
      status(result) shouldEqual CREATED
    }

    "create settings" in {
      val settings = CreateUserSettings("theUserId", 155, 270, 200)
      val json = Json.toJson(settings)
      val result = route(app, FakeRequest(POST, s"$usersApi/${settings.userId}/settings", jsonHeader, json)).get
      status(result) shouldBe CREATED
    }

  }

}

trait UserSpecFixture extends UserFixture {
  val userJson = Json.toJson(user)
  val jsonMimeType = Some(MimeTypes.JSON)
  val jsonHeader = FakeHeaders(Seq(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON))
  val usersApi = "/api/v1/users"
}
