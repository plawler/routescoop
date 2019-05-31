package metrics

import fixtures.CriticalPowerFixture

import org.scalatest.{FlatSpec, Matchers}

class CriticalPowerSolverSpec extends FlatSpec with Matchers with CriticalPowerFixture {

  val cps = MonodScherrerSolver(samples)

  it should "create observations from power efforts" in {
    val observations = Seq(Observation(180.0,64980.0), Observation(360.0,121320.0), Observation(720.0,206640.0))
    cps.observations shouldEqual observations
  }

  it should "calculate critical power" in {
    cps.criticalPower shouldEqual 258.7d
  }

  it should "calculate w prime" in {
    cps.wPrime shouldEqual 22320
  }

  it should "predict power for a given time" in {
    cps.predict(180) shouldEqual 383.0d
    cps.predict(3600) shouldEqual 265.0d
  }

}
