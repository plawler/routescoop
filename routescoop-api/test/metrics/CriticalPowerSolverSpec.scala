package metrics

import fixtures.PowerEffortFixture

import org.scalatest.{FlatSpec, Matchers}

class CriticalPowerSolverSpec extends FlatSpec with Matchers with CpFixture {

  it should "create observations from power efforts" in {
    println(cps.observations)
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
//    for {
//      second <- 45 to 3065
//    } yield {
//      println(cps.predict(second))
//    }
  }

}

trait CpFixture extends PowerEffortFixture {
  val p1 = samplePowerEffort.copy(intervalLengthInSeconds = 180, avgHeartRate = 180, criticalPower = 361, normalizedPower = Some(362))
  val p2 = samplePowerEffort.copy(intervalLengthInSeconds = 360, avgHeartRate = 178, criticalPower = 337, normalizedPower = Some(341))
  val p3 = samplePowerEffort.copy(intervalLengthInSeconds = 720, avgHeartRate = 168, criticalPower = 287, normalizedPower = Some(303))
  val samples = Seq(p1, p2, p3)
  val cps = MonodScherrerSolver(samples)
}
