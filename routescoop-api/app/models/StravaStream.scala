package models

import java.util.UUID

import anorm.{Macro, RowParser}


case class StravaStream (
  id: String,
  activityId: String,
  timeIndexInSeconds: Int,
  latitude: Option[Double] = None,
  longitude: Option[Double] = None,
  distanceMeters: Option[Double] = None,
  altitudeMeters: Option[Double] = None,
  temperatureCelsius: Option[Int] = None,
  grade: Option[Double] = None,
  velocityMetersPerSecond: Option[Double] = None,
  heartRate: Option[Int] = None,
  cadence: Option[Int] = None,
  watts: Option[Int] = None,
  moving: Option[Boolean] = None
)

object StravaStream {

  implicit val parser: RowParser[StravaStream] = Macro.namedParser[StravaStream]

  def createFromScrava(activity: StravaActivity, rawStreamData: Map[String,Any]): StravaStream = {
    // List(latlng, time, distance, altitude, heartrate, cadence, watts, temp, grade_smooth, moving, velocity_smooth)
    StravaStream(
      UUID.randomUUID().toString,
      activity.id,
      rawStreamData("time").asInstanceOf[Int],
      rawStreamData get "latlng" map (_.asInstanceOf[List[Float]].head.toDouble),
      rawStreamData get "latlng" map (_.asInstanceOf[List[Float]].last.toDouble),
      rawStreamData get "distance" map (_.asInstanceOf[Float].toDouble),
      rawStreamData get "altitude" map (_.asInstanceOf[Float].toDouble),
      rawStreamData get "temp" map (_.asInstanceOf[Int]),
      rawStreamData get "grade_smooth" map (_.asInstanceOf[Float].toDouble),
      rawStreamData get "velocity_smooth" map (_.asInstanceOf[Float].toDouble),
      rawStreamData get "heartrate" map (_.asInstanceOf[Int]),
      rawStreamData get "cadence" map (_.asInstanceOf[Int]),
      rawStreamData get "watts" map (_.asInstanceOf[Int]),
      rawStreamData get "moving" map (_.asInstanceOf[Boolean])
    )
  }

}
