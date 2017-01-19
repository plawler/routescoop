package services

import fixtures.UserFixture
import repositories.StravaActivityStore

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ActivityServiceSpec extends WordSpec with Matchers with MockitoSugar with UserFixture with ScalaFutures {

  val mockActivityStore: StravaActivityStore = mock[StravaActivityStore]
  val mockUserService: UserService = mock[UserService]

  val application: Application = new GuiceApplicationBuilder()
    .overrides(bind[StravaActivityStore].toInstance(mockActivityStore))
    .overrides(bind[UserService].toInstance(mockUserService))
    .build

  val service: ActivityService = application.injector.instanceOf(classOf[ActivityService])

  "The Strava Service" should {

    "sync a user's activities" in {
      when(mockUserService.getUser(stravaUser.id)).thenReturn(Future.successful(Some(stravaUser)))
      when(mockActivityStore.findByUserId(stravaUser.id)).thenReturn(Nil)
      Await.result(service.syncActivities(stravaUser.id), 60 seconds)
    }

  }

}
