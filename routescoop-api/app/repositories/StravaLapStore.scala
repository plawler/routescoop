package repositories

import javax.inject.{Inject, Singleton}
import anorm._

import models.StravaLap
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext

/**
  * Created by paullawler on 4/3/17.
  */
trait StravaLapStore {

  val StravaLapsTable = "strava_laps"

  def destroy(): Unit

  def insert(stravaLap: StravaLap): Unit

  def findById(id: String): Option[StravaLap]

}

@Singleton
class StravaLapStoreImpl @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends StravaLapStore {

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$StravaLapsTable
      """.execute()
  }

  override def insert(stravaLap: StravaLap): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$StravaLapsTable (
            id,
            activityId,
            stravaId,
            stravaActivityId,
            athleteId,
            resourceState,
            name,
            elapsedTime,
            movingTime,
            startedAt,
            distance,
            startIndex,
            endIndex,
            lapIndex,
            totalElevationGain,
            averageSpeed,
            maxSpeed,
            averageWatts,
            deviceWatts,
            averageCadence,
            averageHeartRate,
            maxHeartRate
          ) VALUES (
            ${stravaLap.id},
            ${stravaLap.activityId},
            ${stravaLap.stravaId},
            ${stravaLap.stravaActivityId},
            ${stravaLap.athleteId},
            ${stravaLap.resourceState},
            ${stravaLap.name},
            ${stravaLap.elapsedTime},
            ${stravaLap.movingTime},
            ${stravaLap.startedAt},
            ${stravaLap.distance},
            ${stravaLap.startIndex},
            ${stravaLap.endIndex},
            ${stravaLap.lapIndex},
            ${stravaLap.totalElevationGain},
            ${stravaLap.averageSpeed},
            ${stravaLap.maxSpeed},
            ${stravaLap.averageWatts},
            ${stravaLap.deviceWatts},
            ${stravaLap.averageCadence},
            ${stravaLap.averageHeartRate},
            ${stravaLap.maxHeartRate}
          )
      """.executeInsert()
  }

  override def findById(id: String): Option[StravaLap] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$StravaLapsTable WHERE id = $id
      """.as(StravaLap.parser.singleOpt)
  }
}