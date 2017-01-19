package fixtures

import models.StravaActivity

import java.time.Instant

trait ActivityFixture extends UserFixture {
  val activityStartedAt = Instant.now
  val sampleActivity = StravaActivity(
    "theActivityId",
    user.id,
    stravaId = 99999999,
    athleteId = 1234567890,
    name = "My Awesome Ride",
    distance = 48280.3,
    movingTime = 5400,
    elapsedTime = 5400,
    totalElevationGain = 2000,
    activityType = "Ride",
    startedAt = activityStartedAt,
    timezone = "(GMT-05:00) America/New_York",
    startLat = 34.139829,
    startLong = 84.216899,
    trainer = false,
    commute = false,
    manual = false,
    averageSpeed = 17.1,
    maxSpeed = 36.7,
    externalId = Some("2012-12-12_21-40-32-80-29011.fit")
  )
}
