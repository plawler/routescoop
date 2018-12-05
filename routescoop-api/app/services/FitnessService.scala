package services

import javax.inject.{Inject, Singleton}
import metrics.PowerMetrics.trainingLoad
import models.{DailyStress, DailyTrainingLoad}
import modules.NonBlockingContext
import repositories.ActivityStatsStore

import com.typesafe.scalalogging.LazyLogging

import java.text.DecimalFormat
import scala.concurrent.ExecutionContext

@Singleton
class FitnessService @Inject()(activityStatsStore: ActivityStatsStore)
  (implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

  val f = new DecimalFormat("#.#")

  def getDailyTrainingLoad(userId: String, numberOfDays: Int): Seq[DailyTrainingLoad] = {
    val stresses = activityStatsStore.getDailyStress(userId)
    calculateTrainingLoad(stresses).takeRight(numberOfDays) // start from the latest day
  }

  def calculateTrainingLoad(
    dailyStressScores: Seq[DailyStress],
    startingFitness: Option[Double] = None,
    startingFatigue: Option[Double] = None
  ): Seq[DailyTrainingLoad] = {
    // used to initialized the accumulator in the foldLeft
    val start = DailyTrainingLoad(
      dailyStressScores.head.day.minusDays(1), // the day prior to the first daily stress score
      startingFitness.getOrElse(0.0),
      startingFatigue.getOrElse(0.0),
      rampRate = 0.0,
      stressBalance = 0.0)

    dailyStressScores.foldLeft(Seq(start)) { (acc, stress) =>
      val yFitness = acc.last.fitness
      val yFatigue = acc.last.fatigue
      val fitness = trainingLoad(yFitness, stress.stressScore, 42)
      val fatigue = trainingLoad(yFatigue, stress.stressScore, 7)
      acc :+ DailyTrainingLoad(
        stress.day,
        fitness,
        fatigue,
        f.format(fitness - yFitness).toDouble,
        f.format(fitness - fatigue).toDouble
      )
    }
  }

}
