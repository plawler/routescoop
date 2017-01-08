package repositories

import java.time.Instant

import fixtures.UserFixture
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * Created by paullawler on 1/5/17.
  */
class DataSyncRequestSqlStoreSpec extends WordSpec with Matchers {

  val application: Application = new GuiceApplicationBuilder().build()
  val userStore: UserStore = application.injector.instanceOf(classOf[UserStore])
  val dataSyncRequestStore: DataSyncRequestStore = application.injector.instanceOf(classOf[DataSyncRequestStore])

  "The DataSyncRequestStore" should {

    "delete all data" in {
      dataSyncRequestStore.destroy()
    }

    "insert a request" in new Fixture {
      dataSyncRequestStore.insert(user.id, Instant.now)
      dataSyncRequestStore.findByUserId(user.id).size shouldBe 1
    }

    "update a request" in new Fixture {
      val storedRequest = dataSyncRequestStore.insert(user.id, Instant.now)
      dataSyncRequestStore.update(storedRequest.id, Instant.now) shouldBe 1
      dataSyncRequestStore.findById(storedRequest.id).foreach(_.completedAt shouldNot be(None))
    }

  }

  trait Fixture extends UserFixture {

  }

}
