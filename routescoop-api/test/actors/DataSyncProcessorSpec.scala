package actors

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import fixtures.UserFixture
import models.UserDataSyncRequest
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import services.DataSyncService

import scala.concurrent.Future

/**
  * Created by paullawler on 1/21/17.
  */
class DataSyncProcessorSpec extends TestKit(ActorSystem("data-sync-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar {

  val mockDataSyncService: DataSyncService = mock[DataSyncService]
  val processorRef = TestActorRef(new DataSyncProcessor(mockDataSyncService))

  "The DataSyncProcessor" should {

    "initiate a user data sync" in new UserFixture {
      val request = UserDataSyncRequest(user)
      when(mockDataSyncService.startDataSync(request)).thenReturn(Future.successful(()))
      processorRef ! request
    }

  }

}
