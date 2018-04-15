package services

import fixtures.ActivityFixture
import models.StravaActivity
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class StravaWebServiceSpec extends WordSpec with Matchers with MockitoSugar {

  val mockUserService = mock[UserService]
  val application = new GuiceApplicationBuilder()
    .overrides(bind[UserService].toInstance(mockUserService))
    .build()
  val service = application.injector.instanceOf(classOf[StravaWebService])

  "The Strava Web Service" should {

    "get a list of activities for a user" in new StravaWebServiceFixture {
      val result = Await.result(service.getActivities(stravaUser.id), 5 seconds)
      result should not be empty
    }

    "get streams for an activity" in new StravaWebServiceFixture {
      val result = Await.result(service.getStreams(mockStravaActivity), 5 seconds)
      result should not be empty
      result.size shouldEqual 1748
      result.head.timeIndexInSeconds shouldBe 0
    }

  }

  trait StravaWebServiceFixture extends ActivityFixture {
    val mockStravaActivity = mock[StravaActivity]
    when(mockStravaActivity.userId).thenReturn(stravaUser.id)
    when(mockUserService.getUser(stravaUser.id)).thenReturn(Future.successful(Some(stravaUser)))
    when(mockStravaActivity.stravaId).thenReturn(796833837)
  }

}

