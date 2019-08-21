package repositories

import javax.inject.{Inject, Singleton}
import anorm._
import models.PowerEffort
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext


trait PowerEffortStore {

  val PowerEffortsTable = "power_efforts"
  val ActivitiesTable = "strava_activities"

  def destroy(): Unit

  def insert(effort: PowerEffort): Unit

  def insertBatch(efforts: Seq[PowerEffort]): Int

  def findByActivityId(activityId: String): Seq[PowerEffort]

  def getMaximalEfforts(userId: String, lookBackInDays: Int, intervals: Seq[Int]): Seq[PowerEffort]

}

@Singleton
class PowerEffortStoreImpl @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends PowerEffortStore {

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$PowerEffortsTable
      """.execute()
  }

  override def insert(effort: PowerEffort): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$PowerEffortsTable (
            activityId,
            intervalLengthInSeconds,
            startedAt,
            avgHeartRate,
            criticalPower,
            normalizedPower
          ) VALUES (
            ${effort.activityId},
            ${effort.intervalLengthInSeconds},
            ${effort.startedAt},
            ${effort.avgHeartRate},
            ${effort.criticalPower},
            ${effort.normalizedPower}
          )
      """.executeInsert()
  }

  override def insertBatch(efforts: Seq[PowerEffort]): Int = ???

  override def findByActivityId(activityId: String): Seq[PowerEffort] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$PowerEffortsTable WHERE activityId = $activityId
      """.as(PowerEffort.parser.*)
  }

  override def getMaximalEfforts(
    userId: String,
    lookBackInDays: Int,
    intervals: Seq[Int] = Nil
  ): Seq[PowerEffort] = db.withConnection { implicit conn =>
    SQL"""
         SELECT p.*
         FROM #$PowerEffortsTable p
         JOIN (SELECT pe.intervalLengthInSeconds,
                max(criticalPower) as criticalPower,
                substring_index(group_concat(pe.startedAt order by pe.criticalPower desc), ',', 1) as startedAt
               FROM #$PowerEffortsTable pe
               JOIN #$ActivitiesTable a
                 ON a.id = pe.activityId
                 AND a.userId = $userId
               WHERE pe.intervalLengthInSeconds IN ($intervals)
                 AND pe.startedAt BETWEEN DATE(NOW() - INTERVAL $lookBackInDays DAY) AND NOW()
               GROUP BY pe.intervalLengthInSeconds) AS mmp
           ON mmp.intervalLengthInSeconds = p.intervalLengthInSeconds
           AND mmp.criticalPower = p.criticalPower
           AND mmp.startedAt = p.startedAt
        ORDER BY p.intervalLengthInSeconds
      """.as(PowerEffort.parser.*)
  }
}
