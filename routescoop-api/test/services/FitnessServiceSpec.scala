package services

import fixtures.{CriticalPowerFixture, StressScoreFixture}
import models.DailyTrainingLoad
import repositories.{ActivityStatsStore, PowerEffortStore}

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class FitnessServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "The Fitness Service" should {

    "retrieve the user training load for a period of time" in new Fixture {
      when(mockActivityStatsStore.getDailyStress(userId)).thenReturn(stresses)
      val dailyTrainingLoads = service.getTrainingLoad(userId, days)
      dailyTrainingLoads.size shouldEqual days
    }

    "calculate training load" in new Fixture {
      val dailyTrainingLoads = service.calculateTrainingLoad(stresses, Some(startingCtl), Some(startingAtl))
      dailyTrainingLoads.last shouldBe DailyTrainingLoad(LocalDate.of(2018, 12, 1), 53.1, 50.9, 2.2)
    }

    "calculate weekly ramp rate" in new Fixture {
      when(mockActivityStatsStore.getDailyStress(userId)).thenReturn(stressesWithWeek)
      val rr = service.getRampRate(userId, stressesWithWeek.size)
      rr.sixWeekAvgs.size shouldBe 1
    }

    "calculate critical power" in new Fixture {
      val lookback = 90
      val intervals = Seq(180, 360, 720)
      when(mockPowerEffortStore.getMaximalEfforts(userId, lookback, intervals)).thenReturn(samples)
      val cp = service.getCriticalPower(userId, lookback, intervals)
      cp.predictedPower shouldBe Seq(631.0, 445.0, 383.0, 352.0, 334.0, 306.0, 296.0, 284.0, 278.0, 269.0, 265.0)
      cp.cp shouldBe 258.7
      cp.wPrime shouldBe 22320.0
    }

  }

  trait Fixture extends StressScoreFixture with CriticalPowerFixture{
    val userId = "theUserId"
    val days = stresses.size
    val mockActivityStatsStore = mock[ActivityStatsStore]
    val mockPowerEffortStore = mock[PowerEffortStore]
    val service = new FitnessService(mockActivityStatsStore, mockPowerEffortStore)
  }

}
