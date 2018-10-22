package services

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fixtures.ActivityFixture
import models.UserDataSync
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import repositories.{StoredUserDataSync, UserDataSyncStore, UserSettingsStore, UserStore}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserServiceSpec extends TestKit(ActorSystem("user-service-test"))
  with WordSpecLike // needs to use the trait
  with Matchers
  with MockitoSugar {

  "The User Service" should {

    "get the latest user settings relative to an activity" in new UserServiceTesting {
      when(mockSettingsStore.findLatestFor(userId, sampleActivity.startedAt)).thenReturn(Some(userSettings))
      Await.result(service.getSettingsFor(sampleActivity), 1 second) shouldNot be(None)
    }

    "get most recent settings when none prior to activity" in new UserServiceTesting {
      val activity = twoYearOldActivity
      when(mockSettingsStore.findLatestFor(activity.userId, activity.startedAt)).thenReturn(None)
      when(mockSettingsStore.findByUserId(userId)).thenReturn(Seq(userSettings))
      Await.result(service.getSettingsFor(activity), 1 second) shouldNot be(None)
    }

    "get the latest user data sync" in new UserServiceTesting {
      val sync1 = StoredUserDataSync("sync1", userId, Instant.now)
      val sync2 = sync1.copy(id = "sync2", startedAt = sync1.startedAt.minusSeconds(86400))
      val sync3 = sync2.copy(
        id = "sync3",
        startedAt = sync2.startedAt.minusSeconds(86400),
        completedAt = Some(sync2.startedAt.minusSeconds(86340))
      )

      when(mockDataSyncStore.findByUserId(userId)).thenReturn(Seq(sync1, sync2, sync3))
      val someResult = Await.result(service.lastDataSync(user), 1 second)
      someResult shouldEqual Some(UserDataSync(sync3.id, sync3.userId, sync3.startedAt))

      when(mockDataSyncStore.findByUserId(userId)).thenReturn(Seq())
      val noneResult = Await.result(service.lastDataSync(user), 1 second)
      noneResult shouldBe None
    }

  }

  trait UserServiceTesting extends ActivityFixture {
    val mockUserStore = mock[UserStore]
    val mockDataSyncStore = mock[UserDataSyncStore]
    val mockSettingsStore = mock[UserSettingsStore]
    val userId = sampleActivity.userId
    val service = new UserServiceImpl(mockUserStore, mockDataSyncStore, mockSettingsStore, system)
  }

}
