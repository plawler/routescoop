package controllers

import fixtures.UserFixture
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json
import play.api.test.{FakeHeaders, FakeRequest}
import repositories.UserStore
import play.api.test.Helpers._

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

  "The User API" should {

    "create a user" in {
      val result = route(app, FakeRequest(POST, usersApi, JsonHeader, userJson)).get
      status(result) shouldEqual CREATED
    }

  }

}

trait UserSpecFixture extends UserFixture {
  val userJson = Json.toJson(user)
  val JsonContent = Some(MimeTypes.JSON)
  val JsonHeader = FakeHeaders(Seq(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON))
  val usersApi = "/api/v1/users"
}
