package repositories


import anorm._
import models.StravaActivity
import modules.BlockingContext

import play.api.db.Database

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait StravaActivityStore {

  val StravaActivitiesTable = "strava_activities"

  def destroy(): Unit

  def insert(activity: StravaActivity): Unit

  def findById(id: String): Option[StravaActivity]
  def findByUserId(userId: String): Seq[StravaActivity]
  def findBySyncId(syncId: String): Seq[StravaActivity]
}

@Singleton
class StravaActivityStoreImpl @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends StravaActivityStore {

  def insert(activity: StravaActivity): Unit = db.withTransaction { implicit conn =>
    SQL"""
        INSERT INTO #$StravaActivitiesTable (
          id,
          userId,
          stravaId,
          athleteId,
          name,
          distance,
          movingTime,
          elapsedTime,
          totalElevationGain,
          activityType,
          startedAt,
          timezone,
          startLat,
          startLong,
          trainer,
          commute,
          manual,
          averageSpeed,
          maxSpeed,
          externalId,
          endLat,
          endLong,
          mapPolyLine,
          mapPolyLineSummary,
          averageCadence,
          averageTemp,
          averageWatts,
          weightedAverageWatts,
          kilojoules,
          deviceWatts,
          averageHeartRate,
          maxHeartRate,
          workoutType,
          dataSyncId
        ) VALUES (
          ${activity.id},
          ${activity.userId},
          ${activity.stravaId},
          ${activity.athleteId},
          ${activity.name},
          ${activity.distance},
          ${activity.movingTime},
          ${activity.elapsedTime},
          ${activity.totalElevationGain},
          ${activity.activityType},
          ${activity.startedAt},
          ${activity.timezone},
          ${activity.startLat},
          ${activity.startLong},
          ${activity.trainer},
          ${activity.commute},
          ${activity.manual},
          ${activity.averageSpeed},
          ${activity.maxSpeed},
          ${activity.externalId},
          ${activity.endLat},
          ${activity.endLong},
          ${activity.mapPolyLine},
          ${activity.mapPolyLineSummary},
          ${activity.averageCadence},
          ${activity.averageTemp},
          ${activity.averageWatts},
          ${activity.weightedAverageWatts},
          ${activity.kilojoules},
          ${activity.deviceWatts},
          ${activity.averageHeartRate},
          ${activity.maxHeartRate},
          ${activity.workoutType},
          ${activity.dataSyncId}
        )
      """.executeInsert()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$StravaActivitiesTable
      """.execute()
  }

  override def findById(id: String): Option[StravaActivity] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$StravaActivitiesTable WHERE id = $id
      """.as(StravaActivity.parser.singleOpt)
  }

  override def findByUserId(userId: String): Seq[StravaActivity] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$StravaActivitiesTable WHERE userId = $userId
      """.as(StravaActivity.parser.*)
  }

  override def findBySyncId(syncId: String) = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$StravaActivitiesTable WHERE dataSyncId = $syncId
      """.as(StravaActivity.parser.*)
  }

}
