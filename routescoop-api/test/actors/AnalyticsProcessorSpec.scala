package actors

import fixtures.PowerEffortFixture
import models.{PowerEffortsCreated, StravaDataSyncCompleted, UserSettingsCreated}
import services.PowerAnalysisService

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps


class AnalyticsProcessorSpec extends TestKit(ActorSystem("analytics-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with AnalyticsProcessorFixture {

  override def afterAll() = system.terminate()

  val analysisService = mock[PowerAnalysisService]
  val processorRef = TestActorRef(new AnalyticsProcessor(analysisService))

  "The Analytics Processor" should {

    "create power efforts after activities are synced" in {
      when(analysisService.createPowerEfforts(dataSyncId)).thenReturn(Future.successful(()))
      processorRef ! stravaDataSyncCompleted
    }

    "create the power stats for an activity" in {
      when(analysisService.createActivityStats(sampleActivity)).thenReturn(Future.successful(()))
      processorRef ! powerEffortsCreated
    }

    "recalculate activity power stats after new power settings are saved" in {
      when(analysisService.recalculateActivityStats(yearOldSettings)).thenReturn(Future.successful(()))
      processorRef ! userSettingsCreated
    }

  }

}

trait AnalyticsProcessorFixture extends PowerEffortFixture {
  val dataSyncId = "theDataSyncId"
  val stravaDataSyncCompleted = StravaDataSyncCompleted(dataSyncId)
  val powerEffortsCreated = PowerEffortsCreated(sampleActivity)
  val userSettingsCreated = UserSettingsCreated(yearOldSettings)
}
