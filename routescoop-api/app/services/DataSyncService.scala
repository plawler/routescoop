package services

import java.time.Instant
import javax.inject.Inject

import models.DataSyncRequest
import repositories.DataSyncRequestStore

import scala.concurrent.Future


trait DataSyncService {

  def syncUserData(request: DataSyncRequest): Future[Unit]

}

class StravaSyncService @Inject()(dataSyncRequestStore: DataSyncRequestStore) extends DataSyncService {

  override def syncUserData(request: DataSyncRequest): Future[Unit] = {
    dataSyncRequestStore.insert(request.user.id, Instant.now)
    ???
  }

}