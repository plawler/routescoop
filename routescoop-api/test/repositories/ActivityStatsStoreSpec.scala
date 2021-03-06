package repositories

import fixtures.ActivityFixture
import metrics.PowerMetrics
import models.ActivityStats

import org.scalatest.{Matchers, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder

import java.util.UUID

class ActivityStatsStoreSpec extends WordSpec with Matchers with ActivityStatsFixture {

  val application = new GuiceApplicationBuilder().build()
  val userStore = application.injector.instanceOf(classOf[UserStore])
  val userSettingsStore = application.injector.instanceOf(classOf[UserSettingsStore])
  val activityStore = application.injector.instanceOf(classOf[StravaActivityStore])
  val activityStatsStore = application.injector.instanceOf(classOf[ActivityStatsStore])

  "The ActivityStatsStore" should {

    "delete all data" in {
      activityStatsStore.destroy()
      activityStore.destroy()
      userSettingsStore.destroy()
      userStore.destroy()
    }

    "insert activity stats" in {
      userStore.insert(testUser)
      userSettingsStore.insert(testSettings)
      activityStore.insert(testActivity)
      activityStatsStore.insert(testStats)
    }

    "find activity stats by activity id" in {
      val result = activityStatsStore.findByActivityId(testActivity.id)
      result map (_ shouldEqual testStats)
    }

    "update activity stats" in {
      val updated = testStats.copy(averagePower = 200)
      activityStatsStore.update(updated)
      activityStatsStore.findByActivityId(updated.activityId) map { activity =>
        activity.averagePower shouldBe 200
      }
    }

//    "should collect the daily stress records for n days" in {
//      val days = 60
//      val result = activityStatsStore.getDailyStress(testUser.id, days)
//      result.last.stressScore shouldEqual testStats.stressScore
//      result.size shouldEqual days
//    }

    "collect the daily stress records for all time" in {
      val result = activityStatsStore.getDailyStress(testUser.id)
      result.size shouldEqual 1
    }

  }

}

trait ActivityStatsFixture extends ActivityFixture {
  val testUser = user.copy(id = UUID.randomUUID().toString)
  val testSettings = userSettings.copy(userId = testUser.id)
  val testActivity = sampleActivity.copy(id = UUID.randomUUID().toString, userId = testUser.id)
  val testStats = ActivityStats(
    testActivity.id,
    testSettings.id,
    180,
    200,
    90,
    70.0,
    PowerMetrics.variabilityIndex(200, 180)
  )
}
