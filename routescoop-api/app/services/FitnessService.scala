package services

import javax.inject.{Inject, Singleton}
import metrics.PowerMetrics.trainingLoad
import models.{DailyStress, DailyTrainingLoad, FitnessTrend, RampRate}
import modules.NonBlockingContext
import repositories.ActivityStatsStore

import com.typesafe.scalalogging.LazyLogging

import java.text.DecimalFormat
import scala.concurrent.ExecutionContext

@Singleton
class FitnessService @Inject()(activityStatsStore: ActivityStatsStore)
  (implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

  val f = new DecimalFormat("#.#")

  def getTrainingLoad(userId: String, numberOfDays: Int): Seq[DailyTrainingLoad] = {
    val stresses = activityStatsStore.getDailyStress(userId)
    calculateTrainingLoad(stresses).takeRight(numberOfDays) // need all days to calculate but return only the time period specified
  }

  def getRampRate(userId: String, numberOfDays: Int): RampRate = {
    val stresses = activityStatsStore.getDailyStress(userId).takeRight(numberOfDays)
    weeklyRampRate(stresses)
  }

  // experimental
//  def dailyFitnessTrend(userId: String, numberOfDays: Int): FitnessTrend = {
//    val stresses = activityStatsStore.getDailyStress(userId)
//    val load = calculateTrainingLoad(stresses).takeRight(numberOfDays)
//    val rr = dailyRampRate(stresses.takeRight(numberOfDays)) // unlike load, only need the days specified because it's not cumulative
//    FitnessTrend(load, rr)
//  }

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
        f.format(fitness - fatigue).toDouble
      )
    }
  }

  def weeklyRampRate(dailyStresses: Seq[DailyStress]): RampRate = {
    val weeklyTss = dailyStresses.groupBy(_.week).mapValues(_.map(_.stressScore).sum)
    val tssScores = weeklyTss.toSeq.sortWith(_._1 < _._1).map(_._2)
    calculateRampRate(tssScores, 6)
  }

//  def dailyRampRate(dailyStresses: Seq[DailyStress]): RampRate = {
//    val tssScores = dailyStresses.map(_.stressScore)
//    calculateRampRate(tssScores, 42)
//  }

  private def calculateRampRate(tssScores: Seq[Int], windowSize: Int): RampRate = {
    val sixWeekAvgs = tssScores.sliding(windowSize).toList.map(sixweeks => sixweeks.sum / sixweeks.size)
    val ramps = sixWeekAvgs.sliding(2).toList.map(x => (x.last - x.head) / 10)
    RampRate(sixWeekAvgs, ramps)
  }

}
