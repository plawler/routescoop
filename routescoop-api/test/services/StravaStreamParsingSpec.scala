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

  }

}
