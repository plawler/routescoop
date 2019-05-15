package metrics

import org.apache.commons.math3.stat.regression.SimpleRegression

case class Observation(x: Double, y: Double)

class LinearRegression(val observations: Seq[Observation]) { // makes the default constructor private

  val regression = new SimpleRegression()

  def slope: Double = regression.getSlope

  def intercept: Double = regression.getIntercept

  def rSquared: Double = regression.getRSquare

  def predict(x: Double): Double = regression.predict(x)

}

object LinearRegression {

  def apply(observations: Seq[Observation]): LinearRegression = {
    require(observations.size >= 3, "At least three observations are required to run linear regression")
    val lr = new LinearRegression(observations)
    lr.regression.addData(toData(observations))
    lr
  }

  private def toData(observations: Seq[Observation]): Array[Array[Double]] = {
    val data = Array.ofDim[Double](observations.size, 2)
    observations.zipWithIndex.foreach { case (e, i) => data(i) = Array(e.x, e.y) }
    data
  }

}
