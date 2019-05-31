package controllers

import models.CriticalPower
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
      val cpJson = Json.parse("""{"cp":250,"wPrime":20000,"predictedPower":[1,2,3,4]}""")

      when(mockFitnessService.getCriticalPower(userId, days, intervals))
        .thenReturn(CriticalPower(250, 20000, Seq(1,2,3,4)))

      val result = route(
        app,
        FakeRequest(GET, s"/api/v1/users/$userId/cp/$days?intervals=${intervals.head}&intervals=${intervals(1)}&intervals=${intervals(2)}")
      ).get
      status(result) shouldBe OK
      contentAsJson(result) shouldEqual cpJson
    }

  }

}
