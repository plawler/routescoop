package fixtures

trait CriticalPowerFixture extends PowerEffortFixture {
  val p1 = samplePowerEffort.copy(intervalLengthInSeconds = 180, avgHeartRate = 180, criticalPower = 361, normalizedPower = Some(362))
  val p2 = samplePowerEffort.copy(intervalLengthInSeconds = 360, avgHeartRate = 178, criticalPower = 337, normalizedPower = Some(341))
  val p3 = samplePowerEffort.copy(intervalLengthInSeconds = 720, avgHeartRate = 168, criticalPower = 287, normalizedPower = Some(303))
  val samples = Seq(p1, p2, p3)

  val p4 = samplePowerEffort.copy(intervalLengthInSeconds = 180, avgHeartRate = 180, criticalPower = 421, normalizedPower = Some(362))
  val p5 = samplePowerEffort.copy(intervalLengthInSeconds = 360, avgHeartRate = 178, criticalPower = 385, normalizedPower = Some(341))
  val p6 = samplePowerEffort.copy(intervalLengthInSeconds = 720, avgHeartRate = 168, criticalPower = 371, normalizedPower = Some(303))

  val patrickSamples = Seq(p4, p5, p6)
}
