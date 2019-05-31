package services

import fixtures.{CriticalPowerFixture, StressScoreFixture}
import models.{CriticalPowerPrediction, DailyTrainingLoad}
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
      cp.predictions shouldBe Seq(CriticalPowerPrediction(60,631), CriticalPowerPrediction(120,445), CriticalPowerPrediction(180,383), CriticalPowerPrediction(240,352), CriticalPowerPrediction(300,334), CriticalPowerPrediction(480,306), CriticalPowerPrediction(600,296), CriticalPowerPrediction(900,284), CriticalPowerPrediction(1200,278), CriticalPowerPrediction(2400,269), CriticalPowerPrediction(3600,265))
      cp.cp shouldBe 258.7
      cp.wPrime shouldBe 22320.0
    }

    "calculato patrick's predicted power" in new Fixture {
      val lookback = 90
      val intervals = Seq(180, 360, 720)
      when(mockPowerEffortStore.getMaximalEfforts(userId, lookback, intervals)).thenReturn(patrickSamples)
      val cp = service.getCriticalPower(userId, lookback, intervals)
//      println(s"Patrick's CP: ${cp.cp}")
//      println(s"Patrick's CP predictions: ${cp.predictedPower}")
    }

  }

  trait Fixture extends StressScoreFixture with CriticalPowerFixture {
    val userId = "theUserId"
    val days = stresses.size
    val mockActivityStatsStore = mock[ActivityStatsStore]
    val mockPowerEffortStore = mock[PowerEffortStore]
    val service = new FitnessService(mockActivityStatsStore, mockPowerEffortStore)
  }

}
