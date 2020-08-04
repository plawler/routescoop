package repositories

import fixtures.ActivityFixture
import models.{StravaActivity, StravaStream, User}
import services.ActivityStream

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.util.UUID

class TimeInZoneStoreSpec extends WordSpec with Matchers with BeforeAndAfterAll with TimeInZoneFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val userSettingsStore: UserSettingsStore = application.injector.instanceOf(classOf[UserSettingsStore])
  val activityStore: StravaActivityStore = application.injector.instanceOf(classOf[StravaActivityStore])
  val streamStore: StravaStreamStore = application.injector.instanceOf(classOf[StravaStreamStore])
  val timeInZoneStore: TimeInZoneStore = application.injector.instanceOf(classOf[TimeInZoneStore])

  override def beforeAll(): Unit = {

    streamStore.destroy()
    activityStore.destroy()
    userSettingsStore.destroy()
    userStore.destroy()

    userStore.insert(testUser)
    activityStore.insert(testActivity)
  }

  "The TimeInZoneStore" should {

    "generate power statistics for an athlete's power zones" in {
      val ss = activityStream.toInternalStreams(testActivity)
      ss foreach streamStore.insert
      timeInZoneStore.insertPowerInZone(testActivity.id, 270)
      val inZones = timeInZoneStore.findByActivityId(testActivity.id)
      inZones.size shouldBe 7
      inZones.head shouldBe InZone(testActivity.id,"z1",41.23904786d,3949,0.427612344d)
    }

  }

}

trait TimeInZoneFixture extends ActivityFixture {

  val bigStreamJson = Thread.currentThread.getContextClassLoader.getResourceAsStream("streams_json.txt")
  val activityStream = Json.parse(bigStreamJson).as[ActivityStream]

  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testActivity = sampleActivity.copy(id = UUID.randomUUID().toString, userId = testUser.id)

}
