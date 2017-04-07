package fixtures

import java.time.Instant

import models.StravaLap


trait LapFixture extends ActivityFixture {

  val sampleLap = StravaLap(
    "theSampleLap",
    sampleActivity.id,
    796833837,
    18629225,
    sampleActivity.athleteId,
    2,
    "Lap 1",
    1913,
    1747,
    Instant.parse("2016-10-25T19:29:03Z"),
    10793.4,
    0,
    1747,
    1,
    121.0,
    5.6,
    13.9,
    74.0,
    Some(true),
    Some(113.3),
    Some(124.6),
    Some(151.0)
  )

}
