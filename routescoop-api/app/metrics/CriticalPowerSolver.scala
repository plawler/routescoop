package metrics

import models.PowerEffort
import utils.NumberUtils._

trait CriticalPowerSolver {

  def criticalPower: Double
  def wPrime: Double
  def observations: Seq[Observation]
  def predict(timeInSeconds: Double): Double

}

case class MonodScherrerSolver(efforts: Seq[PowerEffort]) extends CriticalPowerSolver {

  val regression = LinearRegression(toKj(efforts))

  override def criticalPower: Double = truncate(regression.slope, 1)

  override def wPrime: Double = roundUp(regression.intercept, 0)

  override def observations: Seq[Observation] = regression.observations

  override def predict(timeInSeconds: Double): Double = roundUp(regression.predict(timeInSeconds) / timeInSeconds, 0)

  private def toKj(efforts: Seq[PowerEffort]): Seq[Observation] = { // convert watts to energy for linear regression
    efforts map (e => Observation(e.intervalLengthInSeconds, e.watts * e.intervalLengthInSeconds))
  }


}
