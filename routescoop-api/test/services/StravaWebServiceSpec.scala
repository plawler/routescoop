package services

import fixtures.ActivityFixture
import models.{StravaActivity, UserDataSync}

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.{LocalDate, Month, ZoneOffset}
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

    "get a list of all activities for a user" in new StravaWebServiceFixture {
      when(mockUserService.lastDataSync(stravaUser)).thenReturn(Future.successful(None))
      val result = Await.result(service.getRecentActivities(stravaUser.id), 10 seconds)
      result should not be empty
      result should have length 14
    }

    "get a list of all activities for paul" in new StravaWebServiceFixture {
      when(mockUserService.getUser(paul.id)).thenReturn(Future.successful(Some(paul)))
      when(mockUserService.lastDataSync(paul)).thenReturn(Future.successful(None))
      val result = Await.result(service.getRecentActivities(paul.id), 100 seconds)
      result should not be empty
      result should have length 50
    }

    "get a list of recent activities for a user" in new StravaWebServiceFixture {
      when(mockUserService.lastDataSync(stravaUser)).thenReturn(Future.successful(None))
      val result = Await.result(service.getRecentActivities(stravaUser.id), 5 seconds)
      result should not be empty
      result should have length 14
    }

    "get a list of previous activities for a user" in new StravaWebServiceFixture {
      val instant = LocalDate.of(2018, Month.MARCH, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
      val result = Await.result(service.getPreviousActivities(stravaUser.id, instant), 5 seconds)
      result should not be empty
      result should have length 9
    }

    "get laps for an activity" in new StravaWebServiceFixture {
      val result = Await.result(service.getLaps(mockStravaActivity), 5 seconds)
      result should not be empty
      result should have length 1
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

