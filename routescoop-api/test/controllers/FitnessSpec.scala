package controllers

import models.{CriticalPower, CriticalPowerPrediction}
import services.FitnessService

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.libs.json.Json

class FitnessSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  val mockFitnessService = mock[FitnessService]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[FitnessService].toInstance(mockFitnessService))
    .build()

  "The Fitness controller" should {

    "calculate critical power for a given set of intervals" in {
      val userId = "abcd1234"
      val days = 180
      val intervals = Seq(180, 360, 720)
      val cpJson = Json.parse("""{"cp":250,"wPrime":20000,"predictions":[{"duration":1,"watts":500},{"duration":2,"watts":400},{"duration":3,"watts":300},{"duration":4,"watts":200}]}""")
      val predictions = Seq(
        CriticalPowerPrediction(1, 500),
        CriticalPowerPrediction(2, 400),
        CriticalPowerPrediction(3, 300),
        CriticalPowerPrediction(4, 200)
      )

      when(mockFitnessService.getCriticalPower(userId, days, intervals))
        .thenReturn(CriticalPower(250, 20000, predictions))

      val result = route(
        app,
        FakeRequest(GET, s"/api/v1/users/$userId/cp/$days?intervals=${intervals.head}&intervals=${intervals(1)}&intervals=${intervals(2)}")
      ).get
      status(result) shouldBe OK
      contentAsJson(result) shouldEqual cpJson
    }

  }

}
