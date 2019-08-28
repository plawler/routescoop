package services

import fixtures.PowerEffortFixture
import models.{ActivityStats, Effort, PowerEffort, PowerEffortsCreated, UserSettings}
import repositories.{ActivityStatsStore, PowerEffortStore, StravaStreamStore, UserSettingsStore}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class PowerAnalysisServiceSpec extends TestKit(ActorSystem("power-analysis-service-test"))
  with WordSpecLike
  with Matchers
  with MockitoSugar {

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[PowerEffortsCreated])

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

    "create power efforts" in new Fixture {
      val dataSyncId = "theDataSyncId"
      when(mockActivityService.getActivitiesBySync(dataSyncId)).thenReturn(Future.successful(Seq(sampleActivity)))
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
//      mockPowerEffortStore.insert(any(classOf[PowerEffort]))

      service.createPowerEfforts(dataSyncId)

      listener.expectMsgClass(10 seconds, classOf[PowerEffortsCreated])
    }

    "save power efforts" in new Fixture {
      when(mockStreamStore.findByActivityId(sampleActivity.id)).thenReturn(streams)
      val efforts = service.calculatePowerEfforts(sampleActivity)
      service.savePowerEfforts(efforts)
      verify(mockPowerEffortStore, times(streams.size)).insert(any(classOf[PowerEffort]))
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

    "calculate activity stats" in new Fixture {
      val settings = UserSettings("theSettingsId", sampleActivity.userId, 155, 270, 200)
      val efforts = Seq(
        PowerEffort(sampleActivity.id, 900, Instant.now, 150, 220, Some(240)),
        PowerEffort(sampleActivity.id, 1800, Instant.now, 150, 210, Some(220)),
        PowerEffort(sampleActivity.id, 3600, Instant.now, 150, 200, Some(210))
      )

      when(mockPowerEffortStore.findByActivityId(sampleActivity.id)).thenReturn(efforts)

      val stats = service.calculateActivityStats(sampleActivity, settings)

      stats.stressScore shouldBe 60
      stats.intensityFactor shouldEqual 0.78d
    }

    "create activity stats" in new Fixture {
      val settings = UserSettings("theSettingsId", sampleActivity.userId, 155, 270, 200)
      val efforts = Seq(
        PowerEffort(sampleActivity.id, 900, Instant.now, 150, 220, Some(240)),
        PowerEffort(sampleActivity.id, 1800, Instant.now, 150, 210, Some(220)),
        PowerEffort(sampleActivity.id, 3600, Instant.now, 150, 200, Some(210))
      )

      when(mockUserSettingsStore.findLatestUntil(sampleActivity.startedAt, sampleActivity.userId))
        .thenReturn(Some(settings))
      when(mockPowerEffortStore.findByActivityId(sampleActivity.id)).thenReturn(efforts)

      Await.result(service.createActivityStats(sampleActivity), 3 seconds)

      verify(mockStatsStore).insert(any(classOf[ActivityStats]))
    }

    "recalculate activity stats" in new Fixture {
      val start = yearOldSettings.createdAt
      val end = userSettings.createdAt
      val userId = yearOldSettings.userId
      val activities = Seq(yearOldActivity, oneWeekOldActivity, oneDayOldActivity)

      when(mockUserSettingsStore.findEarliestAfter(start, userId)).thenReturn(Some(userSettings))
      when(mockActivityService.findBetween(start, end, userId)).thenReturn(Future.successful(activities))

      activities.foreach { activity =>
        when(mockPowerEffortStore.findByActivityId(activity.id))
          .thenReturn(Seq(PowerEffort(activity.id, 3600, Instant.now, 150, 200, Some(210))))
        when(mockStatsStore.findByActivityId(activity.id))
          .thenReturn(Some(ActivityStats(activity.id, yearOldSettings.id, 200, 225, 100, 0.80, 1.3)))
      }

      Await.result(service.recalculateActivityStats(yearOldSettings), 3 seconds)

      verify(mockStatsStore, times(3)).update(any(classOf[ActivityStats]))
    }

  }

  trait Fixture extends PowerEffortFixture {
    val mockActivityService = mock[StravaActivityService]
    val mockStreamStore = mock[StravaStreamStore]
    val mockPowerEffortStore = mock[PowerEffortStore]
    val mockUserSettingsStore = mock[UserSettingsStore]
    val mockStatsStore = mock[ActivityStatsStore]

    val service = new PowerAnalysisService(
      mockActivityService,
      mockUserSettingsStore,
      mockStreamStore,
      mockPowerEffortStore,
      mockStatsStore,
      system)
  }

}
