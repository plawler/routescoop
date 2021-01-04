package models

import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.Json

class RideSpec extends FreeSpec with Matchers {

  "it should deserialize from json" in new Fixture {
    Json.parse(json).as[Ride]
  }

  trait Fixture {
    val json =
      """
        |{
        |    "id": "46e97ac4-e024-417c-a3f0-2bf364f8bd07",
        |    "userId": "5b8dba213298951dabb098f5",
        |    "stravaId": 4550548453,
        |    "athleteId": 178697,
        |    "name": "Boarstone -1",
        |    "startedAt": "2021-01-01T17:37:04Z",
        |    "distance": 56114.6,
        |    "movingTime": 7200,
        |    "elapsedTime": 7200,
        |    "totalElevationGain": 0,
        |    "activityType": "Ride",
        |    "trainer": true,
        |    "commute": false,
        |    "manual": false,
        |    "averageSpeed": 7.794,
        |    "maxSpeed": 8.3,
        |    "externalId": "trainerroad-c7990996-8a2f-4a0f-8d99-c10b30e18ca0.tcx",
        |    "location": {
        |        "timezone": "(GMT-05:00) America/New_York",
        |        "startLat": 0,
        |        "startLong": 0
        |    },
        |    "powerHr": {
        |        "averageCadence": 88.2,
        |        "averageWatts": 176,
        |        "weightedAverageWatts": 178,
        |        "kilojoules": 1268.7,
        |        "deviceWatts": true,
        |        "averageHeartRate": 140.9,
        |        "maxHeartRate": 158
        |    }
        |}
        |""".stripMargin
  }

}
