package metrics

import java.text.DecimalFormat

case class Result(index: Int, value: Int)

object PowerMetrics {

  val formatter = new DecimalFormat("#.##")
  val formatterSingle = new DecimalFormat("#.#")
  val CtlTimeConstant = 42d // days
  val AtlTimeConstant = 7d // days

  // inspired by https://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list

  /**
    * Function which calculates the Mean Maximum value for a given interval. The interval can also be thought of as a
    * window size slid across the values calculating an average and then taking the largest average.
    *
    * @param values
    * @param interval
    * @return the maximum average value for the interval
    */
  def maxAverage(values: Seq[Int], interval: Int): Int = {
//    rollingAverage(values, interval).max
    rollingAverage(values, interval).max.toInt
  }

  /**
    * Function which calculates the Mean Maximum value for a given interval. The interval can also be thought of as a
    * window size slid across the values calculating an average and then taking the largest average.
    *
    * @param values
    * @param interval
    * @return a tuple containing the maximum average value with its index (zero-based)
    */
  def maxAverageWithIndex(values: Seq[Int], interval: Int): (Int, Int) = {
    val (max, idx) = rollingAverage(values, interval).zipWithIndex.maxBy(_._1) // zipWithIndex is zero-based
    (max.toInt, idx)
  }

  /**
    * Calculates the normalized power function for a series of values
    *
    * Normalized power is as follows -
    * 1. Calculate a 30-second rolling average of the power data
    * 2. Raise these values to the fourth power
    * 3. Average the resulting values
    * 4. Take the fourth root of the result
    *
    * @param values
    * @return Optional integer value of np for values of 30 (seconds) or more
    */
  def normalizedPower(values: Seq[Int]): Option[Int] = {
    if (values.length < 30) {
      None
    } else {
      val thirties = rollingAverage(values, 30)
      val raised = thirties.map(n => scala.math.pow(n, 4))
      val avg = raised.sum / raised.size
      Some(scala.math.pow(avg, 1.0 / 4).toInt)
    }
  }

  /**
    * Calculates the ratio of normalized power to ftp
    *
    * @param normalizedPower
    * @param ftp
    * @return double value of IF
    */
  def intensityFactor(normalizedPower: Int, ftp: Int): Double = {
    val intensity = normalizedPower.toDouble / ftp.toDouble
    formatter.format(intensity).toDouble
  }

  /**
    * Stress score is a calculation of how hard an activity is
    *
    * ((s x NP x IF) / (FTP x 3,600)) x 100 where s = duration in seconds
    * Intensity Factor must be computed first
    *
    * @param durationInSeconds
    * @param np
    * @param ftp
    * @param intensity
    * @return the stress score (also known as TSS)
    */
  def stressScore(durationInSeconds: Int, np: Int, ftp: Int, intensity: Double): Int = {
    (((durationInSeconds * np * intensity) / (ftp * 3600)) * 100).toInt
  }

  /**
    * Calculation of evenly paced an athletes power output was during activity
    *
    * Calculated by dividing Normalized Power by Average Power
    * A properly paced time trial should have a VI value of 1.05 or less while a road race or criterium may have a VI as high as 1.1 or more.
    *
    * @param normalizedPower
    * @param avgPower
    * @return double value of variability index
    */
  def variabilityIndex(normalizedPower: Int, avgPower: Int): Double = {
    if (avgPower == 0 || normalizedPower == 0) {
      0.0
    } else {
      val vi = normalizedPower.toDouble / avgPower.toDouble
      formatter.format(vi).toDouble
    }
  }


  /**
    * Training Load is an exponentially weighted average of TSS per day with an N day Time Constant (TC).
    * In other words it takes into account mostly the last N days of training.
    *
    * Chronic Training Load has a time constant of 42 days and can be thought of as “fitness”.
    *
    * Acute Training Load has a time constant of 7 days and can be thought of as "fatigue".
    *
    * @param priorTrainingLoad
    * @param stressScore
    * @param trainingLoadTimeConstant
    * @return double value of training load score
    */
  def trainingLoad(priorTrainingLoad: Double, stressScore: Double, trainingLoadTimeConstant: Double): Double = {
    val tl = priorTrainingLoad + ((stressScore - priorTrainingLoad) / trainingLoadTimeConstant)
    formatterSingle.format(tl).toDouble
  }

  /**
    * VO2max estimator which uses the ACSM protocol. Appropriate for a cycling ergometer.
    * Maximum aerobic power is based on a ramp test. 5-6 minute max power.
    * Vertical and resting constants are the values of O2 used against resistance and at rest.
    *
    * @param maxAerobicPower
    * @param weightInKg
    * @param verticalConstant
    * @param restingConstant
    * @return
    */
  def estimateVO2max(maxAerobicPower: Int, weightInKg: Double, verticalConstant: Double, restingConstant: Double): Double = {
    verticalConstant * (maxAerobicPower / weightInKg) + restingConstant
  }

  def rollingAverage(values: Seq[Int], interval: Int): Seq[Double] = {
    if (interval == 0) throw new IllegalArgumentException("Cannot calculate with zero interval")
    val first = (values take interval).sum.toDouble / interval
    val subtract = values map (_.toDouble / interval)
    val add = subtract drop interval
    val addAndSubtract = add zip subtract map (as => as._1 - as._2)

    addAndSubtract.foldLeft(Seq(first)) { (acc, add) =>
      (add + acc.head) +: acc
    }.reverse
  }

  // https://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list
  def movingAverage(values: List[Double], period: Int): List[Double] = {
    val first = (values take period).sum / period
    val subtract = values map (_ / period)
    val add = subtract drop period
    val addAndSubtract = add zip subtract map Function.tupled(_ - _)
    val res = addAndSubtract.foldLeft(first :: List.fill(period - 1)(0.0)) { (acc, add) =>
      (add + acc.head) :: acc
    }.reverse
    res
  }

}
