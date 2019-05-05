package controllers

import fixtures.RideFixture
import models.{Profile, RideSyncResultError, RideSyncResultStarted}
import services.RideService

import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import play.api.cache.SyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RidesSpec extends WordSpec with Matchers with MockitoSugar with OneAppPerSuite {

  val mockCache = mock[SyncCacheApi]
  val mockRideService = mock[RideService]

  override lazy val app: play.api.Application = new GuiceApplicationBuilder()
    .overrides(bind[SyncCacheApi].toInstance(mockCache))
    .overrides(bind[RideService].toInstance(mockRideService))
    .build()

  "The Rides controller" should {

    "initiate a ride sync from Strava" in new RidesTesting {
      when(mockRideService.syncStrava(stravaUser)).thenReturn(Future.successful(RideSyncResultStarted(rideSync)))
      val result = route(
        app,
        FakeRequest(POST, "/rides/sync")
          .withFormUrlEncodedBody("fetchOlderRides" -> "false")
          .withSession(session)
      ).get
      status(result) shouldBe OK
      contentAsString(result) should startWith("Sync started")
    }

    "initiate ride sync for older activities" in new RidesTesting {
      when(mockRideService.syncStrava(stravaUser, fetchOlderRides = true))
        .thenReturn(Future.successful(RideSyncResultStarted(rideSync)))
      val result = route(
        app,
        FakeRequest(POST, "/rides/sync")
          .withFormUrlEncodedBody("fetchOlderRides" -> "true")
          .withSession(session)
      ).get
      status(result) shouldBe OK
    }

    "error if no user is found" in new RidesTesting {
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
