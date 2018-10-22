package services

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class StravaSummaryActivityParsingSpec extends WordSpec
  with Matchers
  with MockitoSugar {

  "The Strava Web Service" should {

    "parse a list of summary activities" in {
      val json = Thread.currentThread.getContextClassLoader.getResourceAsStream("activities_json.txt")
      val summaryActivityList = Json.parse(json).as[Seq[SummaryActivity]]
      summaryActivityList.size shouldBe 30
    }
  }

}
