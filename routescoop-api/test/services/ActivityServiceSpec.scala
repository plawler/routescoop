package services

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import fixtures.{ActivityFixture, LapFixture}
import models.{StravaActivity, StravaActivityCreated, StravaLapsCreated}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import repositories.{StravaActivityStore, StravaLapStore}

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
  val mockLapStore: StravaLapStore = mock[StravaLapStore]
  val mockUserService: UserService = mock[UserService]
  val mockStravaWebService: StravaWebService = mock[StravaWebService]
  val service = new StravaActivityService(mockStravaWebService, mockActivityStore, mockLapStore, system)

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[StravaActivityCreated])
  system.eventStream.subscribe(listener.ref, classOf[StravaLapsCreated])

  override def afterAll() = system.terminate()

  "The Strava Service" should {

    "sync a user's activities" in {
      when(mockStravaWebService.getActivities(stravaUser.id)).thenReturn(Future.successful(activities))
      when(mockActivityStore.findByUserId(stravaUser.id)).thenReturn(Nil)

      Await.result(service.syncActivities(stravaUser.id), 60 seconds)

      listener.expectMsgClass(60 seconds, classOf[StravaActivityCreated])

      verify(mockActivityStore).insert(mockStravaActivity)
    }

    "sync only the latest activities" in {
      when(mockStravaWebService.getActivities(stravaUser.id)).thenReturn(Future.successful(activities))
      when(mockActivityStore.findByUserId(stravaUser.id)).thenReturn(activities)

      Await.result(service.syncActivities(stravaUser.id), 60 seconds)

      listener.expectNoMsg()
    }

    "retrieve an activity" in {
      when(mockActivityStore.findById(activityId)).thenReturn(Some(sampleActivity))
      val result = Await.result(service.getActivity(activityId), 3 seconds)
      result shouldEqual Some(sampleActivity)
    }

    "sync an activity's laps" in {
      when(mockActivityStore.findById(activityId)).thenReturn(Some(sampleActivity))
      when(mockStravaWebService.getLaps(sampleActivity)).thenReturn(Future.successful(Seq(sampleLap)))

      val result = Await.result(service.syncLaps(activityId), 3 seconds)

      verify(mockLapStore).insert(sampleLap)
      listener.expectMsgClass(10 seconds, classOf[StravaLapsCreated])
    }

  }

}

trait ActivityServiceFixture extends LapFixture with MockitoSugar {
  val mockStravaActivity = mock[StravaActivity]
  when(mockStravaActivity.id).thenReturn("00000000-0000-0000-1111-000000000001")
  when(mockStravaActivity.userId).thenReturn(stravaUser.id)
  val activities = Seq(mockStravaActivity)
}
