package metrics

import utils.NumberUtils._

import org.scalatest.{FlatSpec, Matchers}

class LinearRegressionSpec extends FlatSpec with Matchers with RegressionFixture {

  it should "find the slope and y intercept of a line" in {
    truncate(model.slope, 1) shouldEqual 265.5d
    roundUp(model.intercept, 0) shouldEqual 20964
    roundUp(model.rSquared, 4) shouldEqual 0.9994d
  }

  it should "predict the y value for x" in {
    roundUp(model.predict(3600), 0) shouldEqual 976899
  }

}

trait RegressionFixture {
  val observations = Seq( // converted values to energy - watts * time_in_seconds
    Observation(180, 64980),
    Observation(300, 103500),
    Observation(600, 182400),
    Observation(1200, 338400)
  )
  val model = new LinearRegression(observations)
}
