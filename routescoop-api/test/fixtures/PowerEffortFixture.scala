package fixtures

import java.util.UUID

import models.{PowerEffort, StravaStream}

import scala.io.Source

trait PowerEffortFixture extends StreamFixture {
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

  // using a day old activity because we set the timestamp to the startedAt + startedAtSecond
  val samplePowerEffort = PowerEffort.create(oneDayOldActivity, 30, 360, 140, 400, None)

  val watts = Thread.currentThread.getContextClassLoader.getResourceAsStream("long_activity_watts.txt")
  val line = Source.fromInputStream(watts).getLines()
  val data = line.mkString.split(",").toSeq.map(_.toInt)

  lazy val longActivityStreams = {

    for {
      second <- 1 to data.size
    } yield {
      StravaStream(
        UUID.randomUUID().toString,
        sampleActivity.toString,
        second,
        heartRate = Some(140),
        watts = Some(data(second - 1))
      )
    }
  }

}
