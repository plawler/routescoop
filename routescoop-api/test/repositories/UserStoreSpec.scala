package repositories

import fixtures.UserFixture
import models.User
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class UserStoreSpec extends WordSpec with Matchers with UserFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])

  "The UserStore" should {

    "delete all data" in {
      userStore.destroy()
    }

    "insert a user" in {
      userStore.insert(user)
      userStore.insert(stravaUser)
    }

    "retrieve a user" in {
      userStore.select(user.id).foreach(_ shouldEqual user)
    }

  }

}
