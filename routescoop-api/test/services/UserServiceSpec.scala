package services

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fixtures.ActivityFixture
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import repositories.{UserDataSyncStore, UserSettingsStore, UserStore}

import scala.concurrent.ExecutionContext.Implicits.global

class UserServiceSpec extends TestKit(ActorSystem("user-service-test"))
  with WordSpecLike // needs to use the trait
  with Matchers
  with MockitoSugar {

  "The User Service" should {

    "get the latest user settings relative to an activity" in new UserServiceTesting {
      when(mockSettingsStore.findLatestFor(userId, sampleActivity.startedAt)).thenReturn(Some(userSettings))
      service.getSettingsFor(sampleActivity) shouldNot be(None)
    }

    "get most recent settings when none prior to activity" in new UserServiceTesting {
      val activity = twoYearOldActivity
      when(mockSettingsStore.findLatestFor(activity.userId, activity.startedAt)).thenReturn(None)
      when(mockSettingsStore.findByUserId(userId)).thenReturn(Seq(userSettings))
      service.getSettingsFor(activity) shouldNot be(None)
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
