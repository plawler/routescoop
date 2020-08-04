package repositories

import javax.inject.Inject
import modules.BlockingContext

import com.typesafe.scalalogging.LazyLogging
import anorm._

import play.api.db.Database
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

trait TimeInZoneStore {

  val ActivityStreamsTable = "strava_streams"
  val InZoneStatsTable = "in_zone_stats"

  def findByActivityId(activityId: String): Seq[InZone]
  def findPowerInZone(activityId: String, ftp: Int): Seq[InZone]
  def insertPowerInZone(activityId: String, ftp: Int): Unit
  def delete(activityId: String): Unit
  def destroy(): Unit

}

case class InZone(activityId: String, zone: String, avgInZone: Double, secondsInZone: Int, pctInZone: Double)

object InZone {
  implicit val parser = Macro.namedParser[InZone]
  implicit val format = Json.format[InZone]
}

class TimeInZoneStoreSql @Inject()(db: Database) (implicit @BlockingContext ec: ExecutionContext)
  extends TimeInZoneStore with LazyLogging {

  override def findByActivityId(activityId: String) = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$InZoneStatsTable WHERE activityId = $activityId ORDER BY zone
      """.as(InZone.parser.*)

  }

  override def findPowerInZone(activityId: String, ftp: Int): Seq[InZone] = db.withConnection { implicit conn =>
    val z1 = ftp * .55d
    val z2 = ftp * .75d
    val z3 = ftp * .87d
    val z3a = ftp * .94d
    val z4 = ftp * 1.05d
    val z5 = ftp * 1.2d
    val z6 = ftp * 1.5d

    SQL"""
          select
            ss.activityId,
            case
              when ss.watts <= zones.z1 then 'z1'
              when ss.watts > zones.z1 and ss.watts <= zones.z2 then 'z2'
              when ss.watts > zones.z2 and ss.watts <= zones.z3 then 'z3'
              when ss.watts > zones.z3 and ss.watts <= zones.z4 then 'z4'
              when ss.watts > zones.z4 and ss.watts <= zones.z5 then 'z5'
              when ss.watts > zones.z5 and ss.watts <= zones.z6 then 'z6'
              when ss.watts > zones.z6 then 'z7'
            end as zone,
            avg(ss.watts) as avgInZone,
            count(*) as secondsInZone,
            count(*) / (select count(*) from #$ActivityStreamsTable where activityId = $activityId) as pctInZone
          from #$ActivityStreamsTable ss
          join (select
                  floor($z1) as z1,
                  floor($z2) as z2,
                  floor($z3) as z3,
                  floor($z3a) as z3a,
                  floor($z4) as z4,
                  floor($z5) as z5,
                  floor($z6) as z6
                ) as zones
          where ss.activityId = $activityId
          group by 1,2
      """.as(InZone.parser.*)
  }

  override def insertPowerInZone(activityId: String, ftp: Int): Unit = db.withTransaction { implicit conn =>
    val z1 = ftp * .55d
    val z2 = ftp * .75d
    val z3 = ftp * .87d
    val z3a = ftp * .94d
    val z4 = ftp * 1.05d
    val z5 = ftp * 1.2d
    val z6 = ftp * 1.5d

    SQL"""
          insert into #$InZoneStatsTable (activityId, zone, avgInZone, secondsInZone, pctInZone)
          select
            ss.activityId,
            case
              when ss.watts <= zones.z1 then 'z1'
              when ss.watts > zones.z1 and ss.watts <= zones.z2 then 'z2'
              when ss.watts > zones.z2 and ss.watts <= zones.z3 then 'z3'
              when ss.watts > zones.z3 and ss.watts <= zones.z4 then 'z4'
              when ss.watts > zones.z4 and ss.watts <= zones.z5 then 'z5'
              when ss.watts > zones.z5 and ss.watts <= zones.z6 then 'z6'
              when ss.watts > zones.z6 then 'z7'
            end as zone,
            avg(ss.watts) as avgInZone,
            count(*) as secondsInZone,
            count(*) / (select count(*) from #$ActivityStreamsTable where activityId = $activityId) as pctInZone
          from #$ActivityStreamsTable ss
          join (select
                  floor($z1) as z1,
                  floor($z2) as z2,
                  floor($z3) as z3,
                  floor($z3a) as z3a,
                  floor($z4) as z4,
                  floor($z5) as z5,
                  floor($z6) as z6
                ) as zones
          where ss.activityId = $activityId
          group by 1,2
      """.executeInsert()
  }

  override def delete(activityId: String): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE from #$InZoneStatsTable WHERE activityId = $activityId
      """.execute()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE from #$InZoneStatsTable
      """.execute()
  }

}
