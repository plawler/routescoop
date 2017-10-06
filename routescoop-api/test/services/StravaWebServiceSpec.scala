package services

import fixtures.ActivityFixture
import models.StravaActivity
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.mockito.Mockito._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class StravaWebServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "The Strava Web Service" should {

    "get a list of activities for a user" in new StravaWebServiceFixture {
      val result = Await.result(service.getActivities(stravaUser.id), 3 seconds)
      result should have length 5
    }

    "get streams for an activity" in new StravaWebServiceFixture {
      val result = Await.result(service.getStreams(mockStravaActivity), 5 seconds)
      result shouldNot be(empty)
      result.head.timeIndexInSeconds shouldBe 0
    }

  }


  trait StravaWebServiceFixture extends ActivityFixture {
    val mockUserService = mock[UserService]
    val service = new ScravaWebService(mockUserService)

    val mockStravaActivity = mock[StravaActivity]
    when(mockStravaActivity.userId).thenReturn(stravaUser.id)
    when(mockUserService.getUser(stravaUser.id)).thenReturn(Future.successful(Some(stravaUser)))
    when(mockStravaActivity.stravaId).thenReturn(796833837)
  }

}

