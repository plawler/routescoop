package repositories

import java.util.UUID

import fixtures.StreamFixture
import models.User
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder


class StravaStreamStoreSpec extends WordSpec with Matchers with StreamStoreFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val activityStore: StravaActivityStore = application.injector.instanceOf(classOf[StravaActivityStore])
  val streamStore: StravaStreamStore = application.injector.instanceOf(classOf[StravaStreamStore])

  "The StravaStreamStore" should {

    "delete all data" in {
      streamStore.destroy()
      activityStore.destroy()
      userStore.destroy()
    }

    "insert a new stream data record" in {
      userStore.insert(testUser)
      activityStore.insert(testActivity)
      streamStore.insert(testStream)
    }

    "insert a batch of stream data records" in {
      val batchOfStreams = 1 to 1 map { n =>
        sampleStream.copy(id = s"${sampleStream.id}$n", activityId = testActivity.id)
      }
      streamStore.insertBatch(batchOfStreams)
    }

  }

}

trait StreamStoreFixture extends StreamFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity = sampleActivity.copy(userId = testUser.id)
  val testStream = sampleStream.copy(activityId = testActivity.id)
}