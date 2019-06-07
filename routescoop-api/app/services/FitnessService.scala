package services

import javax.inject.{Inject, Singleton}
import metrics.MonodScherrerSolver
import metrics.PowerMetrics.trainingLoad
import models._
import modules.NonBlockingContext
import repositories.{ActivityStatsStore, PowerEffortStore}

import com.typesafe.scalalogging.LazyLogging

import java.text.DecimalFormat
import java.time.Instant
import scala.concurrent.ExecutionContext

@Singleton
class FitnessService @Inject()(
  activityStatsStore: ActivityStatsStore,
  powerEffortsStore: PowerEffortStore) (implicit @NonBlockingContext ec: ExecutionContext) extends LazyLogging {

  val f = new DecimalFormat("#.#")
  val durations = Seq(60, 120, 180, 240, 300, 480, 600, 900, 1200, 2400, 3600)

  def getTrainingLoad(userId: String, numberOfDays: Int): Seq[DailyTrainingLoad] = {
    val stresses = activityStatsStore.getDailyStress(userId)
    calculateTrainingLoad(stresses).takeRight(numberOfDays) // need all days to calculate but return only the time period specified
  }

  def getRampRate(userId: String, numberOfDays: Int): RampRate = {
    val stresses = activityStatsStore.getDailyStress(userId).takeRight(numberOfDays)
    weeklyRampRate(stresses)
  }

  def getCriticalPower(userId: String, days: Int, intervals: Seq[Int]): CriticalPower = {
    val samples = powerEffortsStore.getMaximalEfforts(userId, days, intervals)
    MonodScherrerSolver(samples).solveFor(durations)
  }

  def simulateCriticalPower(simulation: Simulation): SimulationResult = {
    val efforts = simulation.parameters.map {
      case (k,v) => PowerEffort("CriticalPowerSimulation", k.toInt, Instant.now, 0, v.toInt)
    }.toSeq
    SimulationResult(CP, MonodScherrerSolver(efforts).solveFor(durations))
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
      stressBalance = 0.0
    )

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

  private def calculateRampRate(tssScores: Seq[Int], windowSize: Int): RampRate = {
    val averages = tssScores.sliding(windowSize).toList.map(scores => scores.sum / scores.size)
    val ramps = averages.sliding(2).toList.map(x => (x.last - x.head) / 10)
    RampRate(averages, ramps)
  }

}
