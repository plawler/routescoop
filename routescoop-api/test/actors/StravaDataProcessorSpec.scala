package actors

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import fixtures.ActivityFixture
import models.{StravaDataSyncStarted, UserDataSync}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import services.ActivityService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by paullawler on 1/21/17.
  */
class StravaDataProcessorSpec extends TestKit(ActorSystem("data-sync-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with StravaDataProcessorFixture {

  val mockActivityService = mock[ActivityService]
  val processorRef = TestActorRef(new StravaActivityProcessor(mockActivityService))

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