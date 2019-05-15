package metrics

import utils.NumberUtils._

import org.scalatest.{FlatSpec, Matchers}

class LinearRegressionSpec extends FlatSpec with Matchers with RegressionFixture {

  it should "find the slope and y intercept of a line" in {
    truncate(model.slope, 1) shouldEqual 265.5d // equal to critical power
    roundUp(model.intercept, 0) shouldEqual 20964 // w' or (awc) is the y intercept
    roundUp(model.rSquared, 4) shouldEqual 0.9994d
  }

  it should "predict the y value for x" in {
    roundUp(model.predict(3600), 0) shouldEqual 976899
  }

  it should "require at least three observations" in {
    the [IllegalArgumentException] thrownBy {
      LinearRegression(observations.dropRight(2))
    } should have message "requirement failed: At least three observations are required to run linear regression"
  }

  it should "return the observations used for calculation" in {
    println(model.observations)
  }

}

trait RegressionFixture {
  val observations = Seq( // converted values to energy - watts * time_in_seconds
    Observation(180, 64980),
    Observation(300, 103500),
    Observation(600, 182400),
    Observation(1200, 338400)
  )
  val model = LinearRegression(observations)
}
