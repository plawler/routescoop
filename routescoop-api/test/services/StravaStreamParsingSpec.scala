package services

import fixtures.StravaStreamJsonFixture
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json

class StravaStreamParsingSpec extends WordSpec with Matchers with MockitoSugar with StravaStreamJsonFixture {

  "The Strava Web Service" should {

    "map strava stream json to an activity stream" in {
      val activityStream = Json.parse(stravaStreamJson).as[ActivityStream]
      activityStream.altitude.data.size shouldEqual 3
    }

    "pivot an activity stream to list of maps keyed by type" in {
      val activityStream = Json.parse(stravaStreamJson).as[ActivityStream]
      val pivoted = activityStream.pivot
      pivoted.size shouldEqual 3

      pivoted.head.values.size shouldEqual 11 // number of stream types produced from strava

      val latLng = pivoted(1)("latLng").asInstanceOf[List[Double]]
      latLng.head shouldEqual 34.07658
      latLng.last shouldEqual -84.273291
    }

    "map strava stream with missing heartrate data" in {
      val noHeartRateStream = Json.parse(stravaStreamJsonNoHeartRate).as[ActivityStream]
      noHeartRateStream.heartRate.data.size shouldEqual 0
      noHeartRateStream.pivot.head.get("heartRate") shouldBe None
    }

    "map strava stream with missing watts data" in {
      val noWattsStream = Json.parse(stravaStreamJsonNoWatts).as[ActivityStream]
      noWattsStream.watts.data.size shouldEqual 0
      noWattsStream.pivot.head.get("watts") shouldBe None
    }

    "handle null string value for watts in stream" in {
      val nullWattsStream = Json.parse(stravaStreamJsonNullWatts).as[ActivityStream]
      println(nullWattsStream.watts.data)
    }

  }

}
