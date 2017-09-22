package actors

import java.time.Instant
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import fixtures.ActivityFixture
import models._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import services.ActivityService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by paullawler on 1/21/17.
  */
class StravaActivityProcessorSpec extends TestKit(ActorSystem("data-sync-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with StravaActivityProcessorFixture {

  val mockActivityService = mock[ActivityService]
  val processorRef = TestActorRef(new StravaActivityProcessor(mockActivityService))

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[StravaDataSyncCompleted])

  override def afterAll() = system.terminate()

  "The StravaActivityProcessor" should {

    "complete if no activities to process" in {
      when(mockActivityService.syncActivities(dataSync)).thenReturn(Future.successful(0))
      processorRef ! started
      listener.expectMsgClass(10 seconds, classOf[StravaDataSyncCompleted])
    }

    "begin fetching activities" in {
      when(mockActivityService.syncActivities(dataSync)).thenReturn(Future.successful(1))
      processorRef ! started
    }

    "fetch activity data" in {
      processorRef ! activityCreated
      verify(mockActivityService).syncActivityDetails(activityCreated.activity)
    }

    "monitor the completion of activity syncs" in {
      processorRef ! activitiesCompleted
      listener.expectMsgClass(10 seconds, classOf[StravaDataSyncCompleted])
    }

  }

}

trait StravaActivityProcessorFixture extends ActivityFixture {
  val dataSync = UserDataSync(UUID.randomUUID().toString, user.id, Instant.now)
  val started = StravaDataSyncStarted(dataSync)
  val activity = sampleActivity.copy(dataSyncId = Some(dataSync.id))
  val activityCreated = StravaActivityCreated(activity)
  val activityCompleted = StravaActivitySyncCompleted(activity)
  val activitiesCompleted = ActivityProcessingCompleted(dataSync.id)
}