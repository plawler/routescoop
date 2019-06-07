package fixtures

import models.{CP, CriticalPower, CriticalPowerPrediction, Simulation, SimulationResult}

trait CriticalPowerFixture extends PowerEffortFixture {
  val p1 = samplePowerEffort.copy(intervalLengthInSeconds = 180, avgHeartRate = 180, criticalPower = 361, normalizedPower = Some(362))
  val p2 = samplePowerEffort.copy(intervalLengthInSeconds = 360, avgHeartRate = 178, criticalPower = 337, normalizedPower = Some(341))
  val p3 = samplePowerEffort.copy(intervalLengthInSeconds = 720, avgHeartRate = 168, criticalPower = 287, normalizedPower = Some(303))
  val samples = Seq(p1, p2, p3)

  val p4 = samplePowerEffort.copy(intervalLengthInSeconds = 180, avgHeartRate = 180, criticalPower = 421, normalizedPower = Some(362))
  val p5 = samplePowerEffort.copy(intervalLengthInSeconds = 360, avgHeartRate = 178, criticalPower = 385, normalizedPower = Some(341))
  val p6 = samplePowerEffort.copy(intervalLengthInSeconds = 720, avgHeartRate = 168, criticalPower = 371, normalizedPower = Some(303))

  val patrickSamples = Seq(p4, p5, p6)

  val simulation = Simulation(
    simulationType = CP,
    parameters = Map(
      180.toString -> 421.toString,
      360.toString -> 385.toString,
      720.toString -> 371.toString
    )
  )
  val simulationResultsJson = """{"simulationType":"CriticalPower","result":{"cp":354.7,"wPrime":11520,"predictions":[{"duration":60,"watts":547},{"duration":120,"watts":451},{"duration":180,"watts":419},{"duration":240,"watts":403},{"duration":300,"watts":394},{"duration":480,"watts":379},{"duration":600,"watts":374},{"duration":900,"watts":368},{"duration":1200,"watts":365},{"duration":2400,"watts":360},{"duration":3600,"watts":358}]}}""".stripMargin
  val simulationResult = SimulationResult(
    CP,
    CriticalPower(
      354.7,
      11520,
      Seq(
        CriticalPowerPrediction(60, 547),
        CriticalPowerPrediction(120, 451),
        CriticalPowerPrediction(180, 419),
        CriticalPowerPrediction(240, 403),
        CriticalPowerPrediction(300, 394),
        CriticalPowerPrediction(480, 379),
        CriticalPowerPrediction(600, 374),
        CriticalPowerPrediction(900, 368),
        CriticalPowerPrediction(1200, 365),
        CriticalPowerPrediction(2400, 360),
        CriticalPowerPrediction(3600, 358)
      )
    )
  )
}
