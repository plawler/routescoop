package services

import javax.inject.{Inject, Singleton}

import play.api.Logger

trait DataSyncService {

  def fetchStravaData(token: String, athleteId: Int): Unit

}

@Singleton
class StravaDataSyncService @Inject()() extends DataSyncService {

  override def fetchStravaData(token: String, athleteId: Int): Unit = {
    Logger.debug("fetching data for athlete...")
  }

}
