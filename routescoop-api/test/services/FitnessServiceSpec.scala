package services

import fixtures.StressScoreFixture
import models.DailyTrainingLoad
import repositories.ActivityStatsStore

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

  }

  trait Fixture extends StressScoreFixture {
    val userId = "theUserId"
    val days = stresses.size
    val mockActivityStatsStore = mock[ActivityStatsStore]
    val service = new FitnessService(mockActivityStatsStore)
  }

}
