package actors

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import fixtures.PowerEffortFixture
import models.{PowerEffortsCreated, StravaStreamsCreated}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import services.PowerAnalysisService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


class AnalyticsProcessorSpec extends TestKit(ActorSystem("analytics-actor-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with AnalyticsProcessorFixture {

  override def afterAll() = system.terminate()

  val service = mock[PowerAnalysisService]
  val processorRef = TestActorRef(new AnalyticsProcessor(service))

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[PowerEffortsCreated])

  "The Analytics Processor" should {

    "create power efforts after strava stream data has been processed" in {
      when(service.createEfforts(sampleActivity)).thenReturn(efforts)
      processorRef ! stravaStreamsCreated
      verify(service).saveEfforts(efforts)
      listener.expectMsgClass(10 seconds, classOf[PowerEffortsCreated])
    }

  }

}

trait AnalyticsProcessorFixture extends PowerEffortFixture {
  val stravaStreamsCreated = StravaStreamsCreated(sampleActivity)
  val efforts = Seq(samplePowerEffort)
}
