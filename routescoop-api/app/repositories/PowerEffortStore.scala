package repositories

import javax.inject.{Inject, Singleton}
import anorm._
import models.PowerEffort
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext


trait PowerEffortStore {

  val PowerEffortsTable = "power_efforts"

  def destroy(): Unit

  def insert(effort: PowerEffort): Unit

  def insertBatch(efforts: Seq[PowerEffort]): Int

  def findByActivityId(activityId: String): Seq[PowerEffort]

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

}
