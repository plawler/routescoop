package services

import fixtures.ActivityFixture
import models.StravaActivity
import repositories.StravaActivityStore

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

class StravaWebServiceSpec extends WordSpec with Matchers with MockitoSugar with ActivityFixture {

  val mockActivityStore: StravaActivityStore = mock[StravaActivityStore]

  val application: Application = new GuiceApplicationBuilder()
    .overrides(bind[StravaActivityStore].toInstance(mockActivityStore))
    .build

  val service: StravaWebService = application.injector.instanceOf(classOf[StravaWebService])

  "The Strava Web Service" should {

    "get a list of activities for a user" in {
      when(mockActivityStore.findByUserId(stravaUser.id)).thenReturn(Nil)
      service.getLatestActivities(stravaUser) should have length 5
    }

    "get a list of latest activities for a user" in new Fixture {
      when(mockActivityStore.findByUserId(stravaUser.id)).thenReturn(Seq(mockActivity))
      service.getLatestActivities(stravaUser) should have length 4
    }

  }

  trait Fixture {
    val mockActivity: StravaActivity = mock[StravaActivity]
    when(mockActivity.stravaId).thenReturn(796833837)
  }

}
