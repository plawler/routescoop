package repositories

import fixtures.ActivityFixture
import models.{StravaActivity, User}

import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.util.UUID

class StravaActivityStoreSpec extends WordSpec with Matchers with ActivityStoreFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val activityStore: StravaActivityStore = application.injector.instanceOf(classOf[StravaActivityStore])

  "The Strava Activity Store" should {

    "delete all data" in {
      activityStore.destroy()
      userStore.destroy()
    }

    "insert a new activity" in {
      userStore.insert(testUser)
      activityStore.insert(testActivity)
    }

    "retrieve an activity by id" in {
      activityStore.findById(testActivity.id).foreach(_.id shouldEqual testActivity.id)
    }

  }

}

trait ActivityStoreFixture extends ActivityFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity: StravaActivity = sampleActivity.copy(userId = testUser.id)
}
