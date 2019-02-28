package services

import fixtures.LapFixture

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json

class StravaLapJsonParsingSpec extends WordSpec with Matchers with MockitoSugar with LapFixture {

  "The Strava lap reader" should {

    "map a strava lap json to a lap effort" in {
      val lap = Json.parse(stravaLapJson).as[LapEffort]
      lap.id shouldBe 7003191095l
    }

    "map strava lap json array to a sequence of lap efforts" in {
      val laps = Json.parse(stravaLapJsonArray).as[Seq[LapEffort]]
      laps.size shouldBe 12
    }
  }

}
