package services

import fixtures.StravaSummaryActivityJsonFixture

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class StravaSummaryActivityParsingSpec extends WordSpec
  with Matchers
  with MockitoSugar
  with StravaSummaryActivityJsonFixture {

  "The Strava Web Service" should {

    "parse a list of summary activities" in {
      val json = Thread.currentThread.getContextClassLoader.getResourceAsStream("activities_json.txt")
      val summaryActivityList = Json.parse(json).as[Seq[SummaryActivity]]
      summaryActivityList.size shouldBe 30
    }

    "parse a cycling activity" in {
      Json.parse(stravaSummaryActivityJson).as[SummaryActivity]
    }

    "parse a non cycling activity" in {
      Json.parse(stravaWalkJson).as[SummaryActivity]
    }

  }

}
