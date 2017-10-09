package services

import java.util.UUID

import fixtures.{PowerEffortFixture, StreamFixture}
import models.{PowerEffort, StravaStream}
import org.scalatest.{Matchers, WordSpec}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.mockito.MockitoSugar
import repositories.{PowerEffortStore, StravaStreamStore}

class PowerAnalysisServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "The Analytics Service" should {

    "calculate normalized power" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
      val efforts = service.createEfforts(sampleActivity)
      // np does not apply until 30 seconds in
      efforts.take(29) foreach(_.normalizedPower shouldBe None)
      efforts.takeRight(efforts.size - 29) foreach(_.normalizedPower shouldBe Some(200))
    }

    "calculate power effort start time" in new Fixture {
      // create a list of streams whose watts rise by 1 each second
      val streamData = streams.map(s => s.copy(watts = Some(s.watts.get + s.timeIndexInSeconds)))
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streamData)

      val efforts = service.createEfforts(sampleActivity)

      val largestEffort = efforts.head // 1 second interval is the largest
      largestEffort.criticalPower shouldEqual 240
      // start time for largest effort in this case should be (activity start time) + (index of largest interval)
      largestEffort.startedAt shouldEqual sampleActivity.startedAt.plusSeconds(streamData.last.timeIndexInSeconds)

      val smallestEffort = efforts.last // 40 second interval is the smallest
      smallestEffort.criticalPower should be < largestEffort.criticalPower
      smallestEffort.startedAt shouldEqual sampleActivity.startedAt.plusSeconds(streamData.head.timeIndexInSeconds)
    }

    "save power efforts" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
      val efforts = service.createEfforts(sampleActivity)
      service.saveEfforts(efforts)
      verify(mockPowerEffortStore, times(40)).insert(any(classOf[PowerEffort]))
    }

  }

  trait Fixture extends PowerEffortFixture { // todo move to EffortFixture
    val mockStreamStore = mock[StravaStreamStore]
    val mockPowerEffortStore = mock[PowerEffortStore]
    val service = new PowerAnalysisService(mockStreamStore, mockPowerEffortStore)
  }

}
