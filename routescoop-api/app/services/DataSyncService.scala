package services

import javax.inject.{Inject, Singleton}
import models.{StravaDataSyncStarted, UserDataSync, UserDataSyncRequest}
import modules.NonBlockingContext
import repositories.{StoredUserDataSync, UserDataSyncStore}

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

trait DataSyncService {
  def sync(request: UserDataSyncRequest): Future[UserDataSync]
}

@Singleton
class StravaDataSyncService @Inject()(dataSyncStore: UserDataSyncStore, actorSystem: ActorSystem)
  (implicit @NonBlockingContext ec: ExecutionContext) extends DataSyncService {

  override def sync(request: UserDataSyncRequest): Future[UserDataSync] = Future {
    val stored = StoredUserDataSync.of(request)
    dataSyncStore.insert(stored)
    val sync = UserDataSync(stored.id, stored.userId, stored.startedAt, request.fetchOlderRides)
    actorSystem.eventStream.publish(StravaDataSyncStarted(sync))
    sync
  }
}
