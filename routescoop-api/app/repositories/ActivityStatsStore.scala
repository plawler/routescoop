package repositories

import models.{ActivityStats, DailyStress}
import modules.BlockingContext
import anorm._
import play.api.db.Database
import javax.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

trait ActivityStatsStore {

  val ActivityStatsTable = "activity_stats"
  val ActivityTable = "strava_activities"
  val DaysTable = "days"

  def insert(activityStats: ActivityStats): Unit
  def destroy(): Unit
  def findByActivityId(activityId: String): Option[ActivityStats]
  def getDailyStress(userId: String, numberOfDays: Int): Seq[DailyStress]
  def getDailyStress(userId: String): Seq[DailyStress]

}

@Singleton
class ActivityStatsStoreSql @Inject()(db: Database)
  (implicit @BlockingContext ec: ExecutionContext) extends ActivityStatsStore with LazyLogging {

  override def insert(stats: ActivityStats): Unit = db.withTransaction { implicit conn =>
    logger.info("Inserting stats...")
    SQL"""
          INSERT INTO activity_stats(
            activityId,
            userSettingsId,
            averagePower,
            normalizedPower,
            stressScore,
            intensityFactor,
            variabilityIndex
          ) VALUES (
            ${stats.activityId},
            ${stats.userSettingsId},
            ${stats.averagePower},
            ${stats.normalizedPower},
            ${stats.stressScore},
            ${stats.intensityFactor},
            ${stats.variabilityIndex}
          )
      """.executeInsert()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$ActivityStatsTable
      """.execute()
  }

  override def findByActivityId(activityId: String): Option[ActivityStats] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$ActivityStatsTable WHERE activityId = $activityId
      """.as(ActivityStats.parser.singleOpt)
  }

  override def getDailyStress(userId: String, numberOfDays: Int): Seq[DailyStress] = db.withConnection { implicit conn =>
    val lookBack = numberOfDays - 1
    SQL"""
          SELECT coalesce(date(a.startedAt), d.dt) AS day, sum(coalesce(s.stressScore, 0)) as stressScore
          FROM #$DaysTable d
          LEFT JOIN #$ActivityTable a
            ON d.dt = DATE(a.startedAt)
            AND a.userId = $userId
          LEFT JOIN #$ActivityStatsTable s
            ON s.activityId = a.id
          WHERE d.dt BETWEEN DATE(now() - INTERVAL $lookBack DAY) AND now()
          GROUP BY day, d.dt
          ORDER BY d.dt
      """.as(DailyStress.parser.*)
  }

  override def getDailyStress(userId: String) = db.withConnection { implicit conn =>
    val mode = 3 // week with > 3 days and starts on a monday
    SQL"""
         select coalesce(date(a.startedAt), d.dt) as day,
            sum(coalesce(s.stressScore, 0)) as stressScore,
            week(coalesce(date(a.startedAt), d.dt), $mode) as week
         from #$DaysTable d
         left join #$ActivityTable a
           on d.dt = date(a.startedAt)
           and a.userId = $userId
         left join  #$ActivityStatsTable s
           on s.activityId = a.id
         where d.dt >= ( select min(date(a2.startedAt))
                         from #$ActivityTable a2
                         where a2.userId = $userId)
           and d.dt <= now()
         group by day, week, d.dt
         order by d.dt
      """.as(DailyStress.parser.*)
  }
}
