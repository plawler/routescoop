package repositories

import fixtures.ActivityFixture
import models.{StravaActivity, User}

import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.Instant
import java.time.temporal.ChronoUnit
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

    "retrieve activity summaries by page" in {
      1 to 9 foreach { count =>
        val activity = sampleActivity.copy(id = s"${activityId + count}", userId = testUser.id)
        activityStore.insert(activity)
      }

      activityStore.fetchPaged(testUser.id, 0, 1).size shouldEqual 1 // page 1 is an offset of zero
      activityStore.fetchPaged(testUser.id, 0, 2).size shouldEqual 2
    }

    "retrieve activities between a start and end timestamp" in {
      val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)
      val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS)
      activityStore.findBetween(yesterday, tomorrow, testUser.id) should have length 10
    }

  }

}

trait ActivityStoreFixture extends ActivityFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity: StravaActivity = sampleActivity.copy(userId = testUser.id)
}
