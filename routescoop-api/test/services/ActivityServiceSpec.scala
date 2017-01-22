package services

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import fixtures.ActivityFixture
import models.{StravaActivity, StravaActivityCreated}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import repositories.StravaActivityStore

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class ActivityServiceSpec extends TestKit(ActorSystem("actvity-service-test"))
  with WordSpecLike // needs to be trait instead
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll
  with ActivityServiceFixture {

  val mockActivityStore: StravaActivityStore = mock[StravaActivityStore]
  val mockUserService: UserService = mock[UserService]
  val mockStravaWebService: StravaWebService = mock[StravaWebService]
  val service = new StravaActivityService(mockUserService, mockStravaWebService, mockActivityStore, system)

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[StravaActivityCreated])

  override def afterAll() = system.terminate()

  "The Strava Service" should {

    "sync a user's activities" in {
      when(mockUserService.getUser(stravaUser.id)).thenReturn(Future.successful(Some(stravaUser)))
      when(mockStravaWebService.getLatestActivities(stravaUser)).thenReturn(activities)
      mockActivityStore.insert(mockStravaActivity)

      Await.result(service.syncActivities(stravaUser.id), 60 seconds)

      listener.expectMsgClass(60 seconds, classOf[StravaActivityCreated])
    }

  }

}

trait ActivityServiceFixture extends ActivityFixture with MockitoSugar {
  val mockStravaActivity = mock[StravaActivity]
  val activities = Seq(mockStravaActivity)
}
