package repositories

import java.time.Instant
import fixtures.UserFixture
import models.User

import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.util.UUID

/**
  * Created by paullawler on 1/5/17.
  */
class UserDataSyncStoreSpec extends WordSpec with Matchers with UserDataSyncStoreFixture {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val dataSyncStore: UserDataSyncStore = application.injector.instanceOf(classOf[UserDataSyncStore])

  "The DataSyncRequestStore" should {

    "delete all data" in {
      dataSyncStore.destroy()
    }

    "insert a sync record" in {
      userStore.insert(testUser)
      dataSyncStore.insert(testUserDataSync)
    }

    "find a sync record by user" in {
      dataSyncStore.findByUserId(testUser.id) should have length 1
    }

    "update a sync record" in {
      dataSyncStore.update(testUserDataSync.id, Instant.now) shouldBe 1
      dataSyncStore.findById(testUserDataSync.id).foreach(_.completedAt shouldNot be(None))
    }

  }

}

trait UserDataSyncStoreFixture extends UserFixture {
  val testUser: User = user.copy(id = UUID.randomUUID().toString)
  val testUserDataSync = StoredUserDataSync(UUID.randomUUID().toString, testUser.id, startedAt = Instant.now)
}
