package metrics

import org.apache.commons.math3.stat.regression.SimpleRegression

case class Observation(x: Double, y: Double)

class LinearRegression private { // makes the default constructor private

  val regression = new SimpleRegression()

  def this(observations: Seq[Observation]) = { // bootstrap the regression with data
    this()
    require(observations.size >= 3, "At least three observations are required to run linear regression")
    val data = Array.ofDim[Double](observations.size, 2)
    observations.zipWithIndex.foreach { case (e, i) => data(i) = Array(e.x, e.y) }
    regression.addData(data)
  }

  def slope: Double = regression.getSlope

  def intercept: Double = regression.getIntercept

  def rSquared: Double = regression.getRSquare

  def predict(x: Double): Double = regression.predict(x)

}
