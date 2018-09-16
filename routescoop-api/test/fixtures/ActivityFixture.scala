package fixtures

import java.time.Instant
import java.time.temporal.ChronoUnit

import models.StravaActivity

trait ActivityFixture extends UserFixture {
  val activityId = "theActivityId"
  val activityStartedAt = Instant.now

  val sampleActivity = StravaActivity(
    activityId,
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
    trainer = false,
    commute = false,
    manual = false,
    averageSpeed = 17.1,
    maxSpeed = 36.7,
    externalId = Some("2012-12-12_21-40-32-80-29011.fit"),
    startLat = 34.139829,
    startLong = 84.216899
  )

  val yearOldActivity = sampleActivity.copy(startedAt = activityStartedAt.minus(365, ChronoUnit.DAYS))
  val twoYearOldActivity = sampleActivity.copy(startedAt = activityStartedAt.minus(730, ChronoUnit.DAYS))

}
