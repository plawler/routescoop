package repositories

import java.util.UUID

import fixtures.PowerEffortFixture
import models.User
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class PowerEffortStoreSpec extends WordSpec with Matchers with PowerEffortStoreFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val activityStore: StravaActivityStore = application.injector.instanceOf(classOf[StravaActivityStore])
  val effortStore: PowerEffortStore = application.injector.instanceOf(classOf[PowerEffortStore])

  "The PowerEffortStore" should {

    "delete all data" in {
      activityStore.destroy()
      userStore.destroy()
    }

    "insert a new power effort record" in {
      userStore.insert(testUser)
      activityStore.insert(testActivity)
      effortStore.insert(testEffort)
    }

    "find an effort by activity" in {
      effortStore.findByActivityId(testActivity.id) shouldNot be(None)
      effortStore.findByActivityId(testActivity.id) foreach (_.activityId shouldEqual testEffort.activityId)
    }

    "get maximum power efforts" in {
      effortStore.getMaximalEfforts(testUser.id, 365, Seq(1, 5, 30, 60, 360)) shouldNot be(empty)
      effortStore.getMaximalEfforts(testUser.id, 365, Seq(1, 5, 60, 360)) should be(empty)
    }
    
  }
  
}

trait PowerEffortStoreFixture extends PowerEffortFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity = sampleActivity.copy(userId = testUser.id)
  val testEffort = samplePowerEffort.copy(activityId = testActivity.id)
}
