package services

import fixtures.PowerEffortFixture
import models.{ActivityStats, PowerEffort, UserSettings}
import repositories.{ActivityStatsStore, PowerEffortStore, StravaActivityStore, StravaStreamStore, UserSettingsStore}

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class PowerAnalysisServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "The Power Analysis Service" should {

    "calculate normalized power" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
      val efforts = service.calculatePowerEfforts(sampleActivity)
      // np does not apply until 30 seconds in
      efforts.take(29) foreach (_.normalizedPower shouldBe None)
      efforts.takeRight(efforts.size - 29) foreach (_.normalizedPower shouldBe Some(200))
    }

    "calculate power effort start time" in new Fixture {
      // create a list of streams whose watts rise by 1 each second
      val streamData = streams.map(s => s.copy(watts = Some(s.watts.get + s.timeIndexInSeconds)))
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streamData)

      val efforts = service.calculatePowerEfforts(sampleActivity)

      val largestEffort = efforts.head // 1 second interval is the largest
      largestEffort.criticalPower shouldEqual 240
      // start time for largest effort in this case should be (activity start time) + (index of largest interval)
      largestEffort.startedAt shouldEqual sampleActivity.startedAt.plusSeconds(streamData.last.timeIndexInSeconds)

      val smallestEffort = efforts.last // 40 second interval is the smallest
      smallestEffort.criticalPower should be < largestEffort.criticalPower
      smallestEffort.startedAt shouldEqual sampleActivity.startedAt.plusSeconds(streamData.head.timeIndexInSeconds)
    }

    "calculate power efforts for large streams" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)) thenReturn longActivityStreams
      val start = System.currentTimeMillis()
      val efforts = service.calculatePowerEfforts(sampleActivity)
      val end = System.currentTimeMillis()
      println(s"Calculating power efforts for large streams execution time: ${(end - start) / 1000} seconds")
    }

    "calculate power efforts when stream indices are less than total duration" in new Fixture {
      val longActivityStreamsEvens = longActivityStreams.filter(_.timeIndexInSeconds % 2 == 0)
      when(mockStreamStore.findByActivityId(sampleActivity.id)) thenReturn longActivityStreamsEvens
      val efforts = service.calculatePowerEfforts(sampleActivity)
      efforts.last.intervalLengthInSeconds shouldEqual 13320 // the last interval using the 30 second step
    }

    "save power efforts" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
      val efforts = service.calculatePowerEfforts(sampleActivity)
      service.savePowerEfforts(efforts)
      verify(mockPowerEffortStore, times(40)).insert(any(classOf[PowerEffort]))
    }

    "find longest effort" in new Fixture {
      val efforts = Seq(
        PowerEffort(sampleActivity.id, 900, Instant.now, 150, 220, Some(240)),
        PowerEffort(sampleActivity.id, 1800, Instant.now, 150, 210, Some(220)),
        PowerEffort(sampleActivity.id, 3600, Instant.now, 150, 200, Some(210))
      )
      when(mockPowerEffortStore.findByActivityId(sampleActivity.id)).thenReturn(efforts)
      val longest = service.longestEffort(sampleActivity)
      longest.normalizedPower map (_ shouldBe 210)
    }

    "create activity stats for an activity" in new Fixture {
      val settings = UserSettings("theSettingsId", sampleActivity.userId, 155, 270, 200)
      val efforts = Seq(
        PowerEffort(sampleActivity.id, 900, Instant.now, 150, 220, Some(240)),
        PowerEffort(sampleActivity.id, 1800, Instant.now, 150, 210, Some(220)),
        PowerEffort(sampleActivity.id, 3600, Instant.now, 150, 200, Some(210))
      )

      when(mockUserSettingsStore.findLatestUntil(sampleActivity.startedAt, sampleActivity.userId))
        .thenReturn(Some(settings))
      when(mockPowerEffortStore.findByActivityId(sampleActivity.id)).thenReturn(efforts)

      val stats = Await.result(service.createActivityStats(sampleActivity), 3 seconds)
      stats map (_.stressScore shouldBe 60)
      stats map (_.intensityFactor shouldEqual 0.78d)
    }

//    "recalculate activity stats" in new Fixture {
//      val todaySettings = UserSettings("todaySettings", sampleActivity.userId, 155, 270, 200)
//      val lastWeekSettings = UserSettings(
//        "lastWeekSettings",
//        sampleActivity.userId,
//        155,
//        250,
//        200,
//        todaySettings.createdAt.minus(7, ChronoUnit.DAYS)
//      )
//
//      when(mockUserSettingsStore.findEarliestAfter(lastWeekSettings.createdAt, lastWeekSettings.userId))
//          .thenReturn(Some(todaySettings))
//      when(mockActivityStore.findBetween(lastWeekSettings.createdAt, todaySettings.createdAt, todaySettings.userId))
//          .thenReturn(Seq(sampleActivity, oneDayOldActivity, oneWeekOldActivity))
//
//      service.recalculateActivityStats(lastWeekSettings)
//
//      val invocations = 3
//      verify(mockStatsStore, times(invocations)).update(any[ActivityStats])
//    }

  }

  trait Fixture extends PowerEffortFixture {
    val mockStreamStore = mock[StravaStreamStore]
    val mockPowerEffortStore = mock[PowerEffortStore]
    val mockUserSettingsStore = mock[UserSettingsStore]
    val mockStatsStore = mock[ActivityStatsStore]
    val mockActivityStore = mock[StravaActivityStore]
    val service = new PowerAnalysisService(mockUserSettingsStore, mockStreamStore, mockPowerEffortStore, mockStatsStore, mockActivityStore)
  }

}
