package fixtures

import java.util.UUID

import models.{PowerEffort, StravaStream}

trait PowerEffortFixture extends StreamFixture { // todo move to EffortFixture
  val streams = for {
    second <- 1 to 40
  } yield {
    StravaStream(
      UUID.randomUUID().toString,
      sampleActivity.id,
      second,
      heartRate = Some(140),
      watts = Some(200)
    )
  }

  val samplePowerEffort = PowerEffort.create(sampleActivity, 30, 360, 140, 400, None)
}