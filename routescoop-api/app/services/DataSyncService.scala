package services

import models.{StravaDataSyncStarted, UserDataSyncRequest}
import modules.NonBlockingContext
import repositories.{StoredUserDataSync, UserDataSyncStore}

import akka.actor.ActorSystem

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


trait DataSyncService {

  def startDataSync(request: UserDataSyncRequest): Future[Unit]

}

class DataSyncServiceImpl @Inject()(store: UserDataSyncStore, actorSystem: ActorSystem)
  (implicit @NonBlockingContext ec: ExecutionContext) extends DataSyncService {

  override def startDataSync(request: UserDataSyncRequest): Future[Unit] = Future {
    val stored = StoredUserDataSync(UUID.randomUUID().toString, request.user.id, Instant.now)
    store.insert(stored)
    actorSystem.eventStream.publish(StravaDataSyncStarted(stored.id, stored.userId))
  }
//  override def completeDataSync(completed: ): Future[Unit] = Future {
//    dataSyncRequestStore.update(request., Instant.now)
//  }
}
