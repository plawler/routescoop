package models

import java.util.UUID

import anorm.{Macro, RowParser}


case class StravaStream (
  id: String,
  activityId: String,
  timeIndexInSeconds: Int,
  latitude: Option[Float],
  longitude: Option[Float],
  distanceMeters: Option[Float],
  altitudeMeters: Option[Float],
  temperatureCelsius: Option[Int],
  grade: Option[Float],
  velocityMetersPerSecond: Option[Float],
  heartRate: Option[Int],
  cadence: Option[Int],
  watts: Option[Int],
  moving: Option[Boolean]
)

object StravaStream {

  implicit val parser: RowParser[StravaStream] = Macro.namedParser[StravaStream]

  def create(activity: StravaActivity, rawStreamData: Map[String,Any]): StravaStream = {
    // List(latlng, time, distance, altitude, heartrate, cadence, watts, temp, grade_smooth, moving, velocity_smooth)
    StravaStream(
      UUID.randomUUID().toString,
      activity.id,
      rawStreamData("time").asInstanceOf[Int],
      rawStreamData get "latlng" map (_.asInstanceOf[List[Float]].head),
      rawStreamData get "latlng" map (_.asInstanceOf[List[Float]].last),
      rawStreamData get "distance" map (_.asInstanceOf[Float]),
      rawStreamData get "altitude" map (_.asInstanceOf[Float]),
      rawStreamData get "temp" map (_.asInstanceOf[Int]),
      rawStreamData get "grade_smooth" map (_.asInstanceOf[Float]),
      rawStreamData get "velocity_smooth" map (_.asInstanceOf[Float]),
      rawStreamData get "heartrate" map (_.asInstanceOf[Int]),
      rawStreamData get "cadence" map (_.asInstanceOf[Int]),
      rawStreamData get "watts" map (_.asInstanceOf[Int]),
      rawStreamData get "moving" map (_.asInstanceOf[Boolean])
    )
  }

}
