package actors

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import fixtures.PowerEffortFixture
import models.{PowerEffortsCreated, StravaDataSyncCompleted, StravaStreamsCreated}

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import services.{ActivityService, PowerAnalysisService}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class AnalyticsProcessorSpec extends TestKit(ActorSystem("analytics-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with AnalyticsProcessorFixture {

  override def afterAll() = system.terminate()

  val activityService = mock[ActivityService]
  val analysisService = mock[PowerAnalysisService]
  val processorRef = TestActorRef(new AnalyticsProcessor(activityService, analysisService))

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[PowerEffortsCreated])

  "The Analytics Processor" should {

    "create power efforts after activities are synched" in {
      when(activityService.getActivitiesBySync(dataSyncId)).thenReturn(Future.successful(Seq(sampleActivity)))
      when(analysisService.calculatePowerEfforts(sampleActivity)).thenReturn(efforts)
      analysisService.savePowerEfforts(efforts)

      processorRef ! stravaDataSyncCompleted

      listener.expectMsgClass(10 seconds, classOf[PowerEffortsCreated])
    }

  }

}

trait AnalyticsProcessorFixture extends PowerEffortFixture {
  val dataSyncId = "theDataSyncId"
  val stravaStreamsCreated = StravaStreamsCreated(sampleActivity)
  val stravaDataSyncCompleted = StravaDataSyncCompleted(dataSyncId, Instant.now)
  val efforts = Seq(samplePowerEffort)
}
