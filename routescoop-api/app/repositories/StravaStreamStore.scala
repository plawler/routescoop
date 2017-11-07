package repositories

import javax.inject.Inject
import anorm._

import models.StravaStream
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext


trait StravaStreamStore {

  val StravaStreamsTable = "strava_streams"

  def destroy(): Unit

  def insert(stream: StravaStream): Unit

  def insertBatch(streams: Seq[StravaStream]): Unit

  def findByActivityId(activityId: String): Seq[StravaStream]

}

class StravaStreamStoreImpl @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends StravaStreamStore {

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$StravaStreamsTable
      """.execute()
  }

  override def insert(stream: StravaStream): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$StravaStreamsTable (
            id,
            activityId,
            timeIndexInSeconds,
            latitude,
            longitude,
            distanceMeters,
            altitudeMeters,
            temperatureCelsius,
            grade,
            velocityMetersPerSecond,
            heartRate,
            cadence,
            watts,
            moving
          ) VALUES (
            ${stream.id},
            ${stream.activityId},
            ${stream.timeIndexInSeconds},
            ${stream.latitude},
            ${stream.longitude},
            ${stream.distanceMeters},
            ${stream.altitudeMeters},
            ${stream.temperatureCelsius},
            ${stream.grade},
            ${stream.velocityMetersPerSecond},
            ${stream.heartRate},
            ${stream.cadence},
            ${stream.watts},
            ${stream.moving}
          )
      """.executeInsert()
  }

  override def insertBatch(streams: Seq[StravaStream]): Unit = db.withTransaction { implicit conn =>
    val sql =
      s"""
        INSERT INTO $StravaStreamsTable (
          id,
          activityId,
          timeIndexInSeconds,
          latitude,
          longitude,
          distanceMeters,
          altitudeMeters,
          temperatureCelsius,
          grade,
          velocityMetersPerSecond,
          heartRate,
          cadence,
          watts,
          moving
        ) VALUES (
          {id},
          {activityId},
          {timeIndexInSeconds},
          {latitude},
          {longitude},
          {distanceMeters},
          {altitudeMeters},
          {temperatureCelsius},
          {grade},
          {velocityMetersPerSecond},
          {heartRate},
          {cadence},
          {watts},
          {moving}
        )
      """

    val parameters = streams.map { s =>
      Seq[NamedParameter](
        'id -> s.id,
        'activityId -> s.activityId,
        'timeIndexInSeconds -> s.timeIndexInSeconds,
        'latitude -> s.latitude,
        'longitude -> s.longitude,
        'distanceMeters ->  s.distanceMeters,
        'altitudeMeters -> s.altitudeMeters,
        'temperatureCelsius -> s.temperatureCelsius,
        'grade -> s.grade,
        'velocityMetersPerSecond -> s.velocityMetersPerSecond,
        'heartRate -> s.heartRate,
        'cadence -> s.cadence,
        'watts -> s.watts,
        'moving -> s.moving
      )
    }

    BatchSql(sql, parameters.head, parameters.tail: _*).execute()
  }

  override def findByActivityId(activityId: String) = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$StravaStreamsTable WHERE activityId = $activityId order by timeIndexInSeconds
      """.as(StravaStream.parser.*)
  }

  private def fastBatch(streams: Seq[StravaStream]) = ??? // https://stackoverflow.com/questions/24573242/batch-insert-with-table-that-has-many-columns-using-anorm

}