package controllers

import fixtures.RideFixture
import models.{Profile, RideSyncResultError, RideSyncResultStarted}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.CacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.RideService

import scala.concurrent.Future

class RidesSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  val mockCache = mock[CacheApi]
  val mockRideService = mock[RideService]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].toInstance(mockCache))
    .overrides(bind[RideService].toInstance(mockRideService))
    .build()

  "The Rides controller" should {

    "initiate a ride sync from Strava" in new RidesTesting {
      when(mockRideService.syncStrava(stravaUser)).thenReturn(Future.successful(RideSyncResultStarted(rideSync)))
      val result = route(app, FakeRequest(POST, "/rides/sync").withSession(session)).get
      status(result) shouldBe OK
      contentAsString(result) should startWith("Sync started")
    }

    "errors if no user is found" in new RidesTesting {
      when(mockRideService.syncStrava(stravaUser)).thenReturn(Future.successful(RideSyncResultError("user not found")))
      val result = route(app, FakeRequest(POST, "/rides/sync").withSession(session)).get
      status(result) shouldBe OK
      contentAsString(result) should startWith("Sync error")
    }

  }

  trait RidesTesting extends RideFixture {
    val stravaUser = profileWithStrava.toUser
    when(mockCache.get[Profile](profileKey)).thenReturn(Some(profileWithStrava))
  }

}
