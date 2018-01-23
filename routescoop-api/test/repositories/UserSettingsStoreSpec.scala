package repositories

import fixtures.UserFixture
import models.{CreateUserSettings, UserSettings}
import org.scalatest.{Matchers, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder

class UserSettingsStoreSpec extends WordSpec with Matchers with UserFixture {

  val application = new GuiceApplicationBuilder().build()
  val userStore = application.injector.instanceOf(classOf[UserStore])
  val userSettingsStore = application.injector.instanceOf(classOf[UserSettingsStore])

  "The UserSettingsStore" should {

    "delete all data" in {
      userStore.destroy()
      userSettingsStore.destroy()
    }

    "insert a user settings" in new UserSettingsFixture {
      userStore.insert(user)
      userSettingsStore.insert(settings)
    }

    "retrieve a user settings" in new UserSettingsFixture {
      userSettingsStore.findById(settings.id) foreach(_ shouldEqual settings)
    }

    "retrieve all user settings for a user" in new UserSettingsFixture {
      val settingsList = userSettingsStore.findByUserId(user.id)
      settingsList shouldEqual Seq(settings)
    }

    "delete a user settings" in new UserSettingsFixture {
      userSettingsStore.delete(settings.id)
      userSettingsStore.findById(settings.id) foreach(_ shouldBe None)
    }

  }

}

trait UserSettingsFixture extends UserFixture {
  val settings = UserSettings.of(CreateUserSettings(user.id, 155, 285, 200))
}