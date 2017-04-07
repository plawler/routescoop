package services

import fixtures.ActivityFixture
import models.StravaActivity
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class StravaWebServiceSpec extends WordSpec with Matchers with MockitoSugar with ActivityFixture {

  val mockUserService = mock[UserService]
  val service = new ScravaWebService(mockUserService)

  "The Strava Web Service" should {

    "get a list of activities for a user" in {
      when(mockUserService.getUser(stravaUser.id)).thenReturn(Future.successful(Some(stravaUser)))
      val result = Await.result(service.getActivities(stravaUser.id), 3 seconds)
      result should have length 5
    }

  }

  trait Fixture {
    val mockActivity: StravaActivity = mock[StravaActivity]
    when(mockActivity.stravaId).thenReturn(796833837)
  }

}
