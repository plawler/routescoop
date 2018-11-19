package fixtures

import java.time.Instant
import java.util.UUID

import models.{RideSummary, RideSync}
import play.api.libs.json.Json

trait RideFixture extends ProfileFixture {
  val rideSync = RideSync(UUID.randomUUID().toString, profileWithStrava.id, Instant.now)
  val rideSummaryPageJson =
    """
      |[
      |    {
      |        "id": "844afa37-9e0e-42b9-9c2c-b36d4c93fa15",
      |        "name": "Morning Ride",
      |        "startedAt": "2018-11-11T14:07:05Z",
      |        "distance": 73611.3,
      |        "movingTime": 10375,
      |        "analysisCompleted": true
      |    },
      |    {
      |        "id": "f9fb1e90-dc82-410f-8b79-6c0430eba2a2",
      |        "name": "Morning Ride",
      |        "startedAt": "2018-11-10T15:13:59Z",
      |        "distance": 0,
      |        "movingTime": 5400,
      |        "analysisCompleted": true
      |    },
      |    {
      |        "id": "de1a1ee1-bf4e-4060-94bb-bd657afb41e7",
      |        "name": "Evening Ride",
      |        "startedAt": "2018-11-10T00:00:09Z",
      |        "distance": 0,
      |        "movingTime": 2700,
      |        "analysisCompleted": true
      |    }
      |]
    """.stripMargin

  val summaries = Json.parse(rideSummaryPageJson).as[Seq[RideSummary]]
}
