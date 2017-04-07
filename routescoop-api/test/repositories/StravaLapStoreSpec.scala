package repositories

import java.util.UUID

import fixtures.LapFixture
import models.{StravaActivity, User}
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder


class StravaLapStoreSpec extends WordSpec with Matchers with LapStoreFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val activityStore: StravaActivityStore = application.injector.instanceOf(classOf[StravaActivityStore])
  val lapStore = application.injector.instanceOf(classOf[StravaLapStore])


  "The Strava Lap Store" should {

    "delete all laps" in {
      lapStore.destroy()
      activityStore.destroy()
      userStore.destroy()
    }

    "insert a new lap" in {
      userStore.insert(testUser)
      activityStore.insert(testActivity)
      lapStore.insert(testLap)
    }

    "find a lap by id" in {
      lapStore.findById(testLap.id).foreach(_.id shouldEqual testLap.id)
    }

  }

}

trait LapStoreFixture extends LapFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity: StravaActivity = sampleActivity.copy(userId = testUser.id)
  val testLap = sampleLap.copy(activityId = testActivity.id)
}
