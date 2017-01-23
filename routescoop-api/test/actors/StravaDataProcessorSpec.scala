package actors

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import fixtures.{ActivityFixture, UserFixture}
import models.{StravaDataSyncStarted, UserDataSync}
import org.mockito.Mockito._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.mock.MockitoSugar
import services.ActivityService

import scala.concurrent.Future

/**
  * Created by paullawler on 1/21/17.
  */
class StravaDataProcessorSpec extends TestKit(ActorSystem("data-sync-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with StravaDataProcessorFixture {

  val mockActivityService = mock[ActivityService]
  val processorRef = TestActorRef(new StravaDataProcessor(mockActivityService))

  "The StravaDataProcessor" should {

    "begin fetching activities" in {
      when(mockActivityService.syncActivities(user.id)).thenReturn(Future.successful(()))
      processorRef ! started
    }

  }

}

trait StravaDataProcessorFixture extends ActivityFixture {
  val dataSync = UserDataSync(UUID.randomUUID().toString, user.id, Instant.now)
  val started = StravaDataSyncStarted(dataSync)
}