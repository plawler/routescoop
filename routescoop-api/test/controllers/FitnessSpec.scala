package controllers

import fixtures.CriticalPowerFixture
import models._
import services.FitnessService

import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HeaderNames, MimeTypes}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.libs.json.Json

import java.time.Instant
import scala.util.Random

class FitnessSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite with CriticalPowerFixture {

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

    "simulate critical power for a set of data points" in {
      val headers = FakeHeaders(Seq(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON))
      val simulation = Simulation(
        CP,
        Map(
          180.toString -> 421.toString,
          360.toString -> 385.toString,
          720.toString -> 371.toString
        )
      )
      when(mockFitnessService.simulateCriticalPower(simulation)).thenReturn(simulationResult)

      val resultJson = Json.parse(simulationResultsJson)
      val result = route(app, FakeRequest(POST, s"/api/v1/simulations", headers, Json.toJson(simulation))).get
      status(result) shouldBe CREATED
      contentAsJson(result) shouldEqual resultJson
    }

    "return the mean maximal power values for a user" in {
      val userId = "1234567890"
      val p = new Random
      val hr = new Random
      val efforts = 1 to 7200 map { i =>
        PowerEffort(Random.alphanumeric.take(10).mkString, i, Instant.now, hr.nextInt(200), p.nextInt(1000))
      }

      when(mockFitnessService.getMeanMaximalPower(userId, None)).thenReturn(efforts)

      val result = route(app, FakeRequest(GET, s"/api/v1/users/$userId/mmp")).get
      status(result) shouldBe OK
    }

  }

}
