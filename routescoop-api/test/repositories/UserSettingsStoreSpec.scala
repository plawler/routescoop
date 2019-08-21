package repositories

import fixtures.UserFixture
import models.UserSettings

import org.scalatest.{Matchers, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.temporal.ChronoUnit

class UserSettingsStoreSpec extends WordSpec with Matchers with UserFixture {

  val application = new GuiceApplicationBuilder().build()
  val userStore = application.injector.instanceOf(classOf[UserStore])
  val userSettingsStore = application.injector.instanceOf(classOf[UserSettingsStore])

  "The UserSettingsStore" should {

    "delete all data" in {
      userStore.destroy()
      userSettingsStore.destroy()
    }

    "insert a user settings" in {
      userStore.insert(user)
      userSettingsStore.insert(userSettings)
    }

    "retrieve a user settings" in {
      userSettingsStore.findById(userSettings.id) foreach (_ shouldEqual userSettings)
    }

    "retrieve all user settings for a user" in {
      val settingsList = userSettingsStore.findByUserId(user.id)
      settingsList shouldNot be(empty)
      settingsList shouldEqual Seq(userSettings)
    }

    "retrieve the latest user settings" in {
      val userId = userSettings.userId
      val today = userSettings.createdAt
      val settingsList = Seq(
        userSettings.copy(id = "minusOne", createdAt = today.minus(1, ChronoUnit.DAYS)),
        userSettings.copy(id = "minusTwo", createdAt = today.minus(2, ChronoUnit.DAYS)),
        userSettings.copy(id = "minusThree", createdAt = today.minus(3, ChronoUnit.DAYS))
      )
      settingsList.foreach(userSettingsStore.insert)
      userSettingsStore.findLatestUntil(today.minus(2, ChronoUnit.DAYS), userId) map {
        _.id shouldEqual "minusTwo"
      }
      userSettingsStore.findLatestUntil(today, userId) map (_.id shouldEqual "theSettingsId")

      userSettingsStore.findEarliestAfter(today.minus(3, ChronoUnit.DAYS), userId) map {
        _.id shouldEqual "minusTwo"
      }

      userSettingsStore.findEarliestAfter(today.minus(2, ChronoUnit.DAYS), userId) map {
        _.id shouldEqual "minusOne"
      }
    }

    "delete a user settings" in {
      userSettingsStore.delete(userSettings.id)
      userSettingsStore.findById(userSettings.id) foreach (_ shouldBe None)
    }

  }

}
