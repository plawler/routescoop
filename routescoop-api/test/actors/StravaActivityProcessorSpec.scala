package actors

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import fixtures.ActivityFixture
import models.{StravaActivityCreated, StravaActivitySyncCompleted, StravaDataSyncStarted, UserDataSync}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import services.ActivityService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by paullawler on 1/21/17.
  */
class StravaActivityProcessorSpec extends TestKit(ActorSystem("data-sync-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with StravaActivityProcessorFixture {

  val mockActivityService = mock[ActivityService]
  val processorRef = TestActorRef(new StravaActivityProcessor(mockActivityService))

  "The StravaDataProcessor" should {

    "begin fetching activities" in {
      when(mockActivityService.syncActivities(dataSync)).thenReturn(Future.successful(1))
      processorRef ! started
    }

    "fetch activity data" in {
      processorRef ! activityCreated
      verify(mockActivityService).syncActivity(activityCreated.activity)
    }

    "monitor the completion of activity syncs" in {
      processorRef ! activitySyncCompleted
    }

  }

}

trait StravaActivityProcessorFixture extends ActivityFixture {
  val dataSync = UserDataSync(UUID.randomUUID().toString, user.id, Instant.now)
  val started = StravaDataSyncStarted(dataSync)
  val activity = sampleActivity.copy(dataSyncId = Some(dataSync.id))
  val activityCreated = StravaActivityCreated(activity)
  val activitySyncCompleted = StravaActivitySyncCompleted(activity)
}