package services

import java.time.Instant
import java.util.{Date, UUID}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import fixtures.{LapFixture, StreamFixture}
import models._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import repositories.{StravaActivityStore, StravaLapStore, StravaStreamStore}

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
  val mockStreamStore = mock[StravaStreamStore]
  val mockUserService: UserService = mock[UserService]
  val mockStravaWebService: StravaWebService = mock[StravaWebService]
  val service = new StravaActivityService(
    mockStravaWebService,
    mockActivityStore,
    mockLapStore,
    mockStreamStore,
    system
  )

  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[StravaActivityCreated])
  system.eventStream.subscribe(listener.ref, classOf[StravaLapsCreated])
  system.eventStream.subscribe(listener.ref, classOf[StravaStreamsCreated])
  system.eventStream.subscribe(listener.ref, classOf[StravaActivitySyncCompleted])


  override def afterAll() = TestKit.shutdownActorSystem(system)

  "The Strava Service" should {

    "sync a user's activities" in {
      when(mockStravaWebService.getRecentActivities(paul.id)).thenReturn(Future.successful(Seq(sampleActivity)))
      when(mockActivityStore.findByUserId(paul.id)).thenReturn(Nil)

      val result = Await.result(service.syncActivities(userDataSync), 60 seconds)
      result shouldEqual 1

      listener.expectMsgClass(60 seconds, classOf[StravaActivityCreated])
      val synchedActivity = sampleActivity.copy(dataSyncId = Some(dataSyncId))
      verify(mockActivityStore).insert(synchedActivity)
    }

    "sync only the latest activities" in {
      when(mockStravaWebService.getRecentActivities(paul.id)).thenReturn(Future.successful(remoteActivities))
      when(mockActivityStore.findByUserId(paul.id)).thenReturn(localActivities)

      Await.result(service.syncActivities(userDataSync), 60 seconds)

      listener.expectMsgClass(60 seconds, classOf[StravaActivityCreated])
      val latestActivitySynched = r3.copy(dataSyncId = Some(dataSyncId))
      verify(mockActivityStore).insert(latestActivitySynched)
    }

    "retrieve an activity" in {
      when(mockActivityStore.findById(activityId)).thenReturn(Some(sampleActivity))
      val result = Await.result(service.getActivity(activityId), 3 seconds)
      result shouldEqual Some(sampleActivity)
    }

    "sync an activity's data" in {
      // have to reset the mocks used in previous verifications
      reset(mockLapStore)
      reset(mockStreamStore)

      when(mockStravaWebService.getLaps(sampleActivity)).thenReturn(Future.successful(Seq(sampleLap)))
      when(mockStravaWebService.getStreams(sampleActivity)).thenReturn(Future.successful(streams))

      val result = Await.result(service.syncActivityDetails(sampleActivity), 3 seconds)

      verify(mockLapStore).insert(sampleLap)
      verify(mockStreamStore).insertBatch(streams)

//      listener.expectMsgClass(10 seconds, classOf[StravaLapsCreated])
//      listener.expectMsgClass(10 seconds, classOf[StravaStreamsCreated])
      listener.expectMsgClass(10 seconds, classOf[StravaActivitySyncCompleted])
    }

    "fetch activities by sync" in {
      when(mockActivityStore.findBySyncId(dataSyncId)).thenReturn(localActivities)
      val result = Await.result(service.getActivitiesBySync(dataSyncId), 3 seconds)
      result.size shouldBe 2
    }

    "calculate page and offset for fetching activities" in {

    }

  }

}

trait ActivityServiceFixture extends LapFixture with StreamFixture with MockitoSugar {
  val dataSyncId = "00000000-0000-0000-0000-000000000001"
  val id1 = "00000000-0000-0000-1111-000000000001"
  val id2 = "00000000-0000-0000-1111-000000000002"

  val userDataSync = UserDataSync(dataSyncId, paul.id, Instant.now)

  val a1 = sampleActivity.copy(id = id1, userId = paul.id, stravaId = 1)
  val a2 = sampleActivity.copy(id = id2, userId = paul.id, stravaId = 2)

  val r1 = sampleActivity.copy(id = UUID.randomUUID().toString, userId = paul.id, stravaId = 1)
  val r2 = sampleActivity.copy(id = UUID.randomUUID().toString, userId = paul.id, stravaId = 2)
  val r3 = sampleActivity.copy(id = UUID.randomUUID().toString, userId = paul.id, stravaId = 3)

  val localActivities = Seq(a1, a2)
  val remoteActivities = Seq(r1, r2, r3)

  val stream1 = sampleStream.copy(id = "stream1", activityId = sampleActivity.id)
  val stream2 = sampleStream.copy(id = "stream2", activityId = sampleActivity.id)
  val streams = Seq(stream1, stream2)

  val summaries = 1 to 10 map { count =>
    Summary(s"activityId$count", s"activity$count", Instant.now(), 2000.0, 1800)
  }
}
