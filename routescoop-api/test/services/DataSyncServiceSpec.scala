package services

import fixtures.UserFixture
import models.{StravaDataSyncStarted, UserDataSyncRequest}
import repositories.{StoredUserDataSync, UserDataSyncStore}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DataSyncServiceSpec extends TestKit(ActorSystem("datasync-service-test"))
  with WordSpecLike // needs to be trait instead
  with Matchers
  with MockitoSugar
  with BeforeAndAfterAll {

  val mockStore = mock[UserDataSyncStore]
  val service = new StravaDataSyncService(mockStore, system)
  val listener = TestProbe()
  system.eventStream.subscribe(listener.ref, classOf[StravaDataSyncStarted])

  override def afterAll() = TestKit.shutdownActorSystem(system)

  "The Strava data sync service" should {

    "initiate a user data sync" in new DataSyncServiceTesting {
      val result = Await.result(service.sync(dsr), 60 seconds)
      result.userId shouldEqual stravaUser.id
      verify(mockStore).insert(any(classOf[StoredUserDataSync]))
      listener.expectMsgClass(61 seconds, classOf[StravaDataSyncStarted])
    }

  }

  trait DataSyncServiceTesting extends UserFixture {
    val dsr = UserDataSyncRequest(stravaUser.id, fetchOlderRides = false)
  }
}
