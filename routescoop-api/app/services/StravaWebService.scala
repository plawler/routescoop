package services

import java.time.Instant

import javax.inject.Inject
import com.google.inject.Singleton
import com.typesafe.scalalogging.LazyLogging
import models._
import modules.{NonBlockingContext, StravaConfig}
import play.api.libs.ws.WSClient
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal


trait StravaWebService {

  def getRecentActivities(userId: String): Future[Seq[StravaActivity]]

  def getPreviousActivities(userId: String, before: Instant): Future[Seq[StravaActivity]]

  def getLaps(activity: StravaActivity): Future[Seq[StravaLap]]

  def getStreams(activity: StravaActivity): Future[Seq[StravaStream]]

}

@Singleton
class StravaWebServiceImpl @Inject()(
  userService: UserService, ws: WSClient, config: StravaConfig)
  (implicit @NonBlockingContext ec: ExecutionContext) extends StravaWebService with LazyLogging {

  val pageSize = config.pageSize

  override def getRecentActivities(userId: String): Future[Seq[StravaActivity]] = {
    getUser(userId) flatMap {
      case Some(user) => getStravaActivities(user)
      case None => Future.successful(Nil)
    }
  }

  override def getPreviousActivities(userId: String, before: Instant): Future[Seq[StravaActivity]] = {
    getUser(userId) flatMap {
      case Some(user) => getStravaActivities(user, Some(before))
      case None => Future.successful(Nil)
    }
  }

  def getLaps(activity: StravaActivity): Future[Seq[StravaLap]] = {
    val url = s"https://www.strava.com/api/v3/activities/${activity.stravaId}/laps"
    getToken(activity.userId) flatMap {
      case Some(token) =>
        ws.url(url).withHttpHeaders("Authorization" -> s"Bearer $token").get() map { response =>
          response.json.validate[Seq[LapEffort]] match {
            case success: JsSuccess[Seq[LapEffort]] =>
              success.get.map(lap => StravaLap.create(activity, lap))
            case error: JsError =>
              logger.error(s"No lap data found for activity ${activity.id}", error)
              logger.error(error.toString)
              Nil
          }
        }
      case None => Future.successful(Nil)
    }
  }

  def getStreams(activity: StravaActivity): Future[Seq[StravaStream]] = {
    val keys = "time,latlng,distance,altitude,velocity_smooth,heartrate,cadence,watts,temp,moving,grade_smooth"
    val url = s"https://www.strava.com/api/v3/activities/${activity.stravaId}/streams?keys=$keys&key_by_type=true"
    getToken(activity.userId) flatMap {
      case Some(token) =>
        ws.url(url).withHttpHeaders("Authorization" -> s"Bearer $token").get() map { response =>
          response.json.validate[ActivityStream] match {
            case success: JsSuccess[ActivityStream] => activityStreamToStravaStreams(activity, success.get)
            case error: JsError =>
              logger.error(s"No stream data found for activity ${activity.id}", error)
              Nil
          }
        } recover {
          case NonFatal(e) =>
            logger.error(s"Failed to retrieve stream data for user ${activity.userId} with token $token", e)
            Nil
        }
      case None => Future.successful(Nil)
    }
  }

  private def getStravaActivities(user: User, before: Option[Instant] = None): Future[Seq[StravaActivity]] = {
    val params = before map { at =>
      s"?before=${(at.toEpochMilli / 1000).toInt}" // strava api requires the timestamp to be an int
    } getOrElse {
      s"?page=1&per_page=$pageSize"
    }

    val url = "https://www.strava.com/api/v3/athlete/activities" + params
    logger.info(s"Strava list activities request url: $url")

    user.stravaToken match {
      case Some(token) =>
        ws.url(url)
          .withHttpHeaders("Authorization" -> s"Bearer $token")
          .withRequestTimeout(10 seconds)
          .get() map { response =>
          response.json.validate[Seq[SummaryActivity]] match {
            case success: JsSuccess[Seq[SummaryActivity]] =>
              success.get map (sa => StravaActivity.create(user, sa))
            case error: JsError =>
              logger.error("Failed to map Strava json activity response", error)
              Nil
          }
        } recover {
          case NonFatal(e) =>
            logger.error(s"Failed to retrieve Strava activity data for user $user", e)
            Nil
        }
      case None =>
        logger.warn(s"User ${user.id} has no Strava token")
        Future.successful(Nil)
    }
  }

  private def getUser(userId: String) = userService.getUser(userId)

  private def getToken(userId: String) = {
    getUser(userId) map {
      case Some(user) => user.stravaToken
      case None => None
    }
  }

  private def activityStreamToStravaStreams(activity: StravaActivity, activityStream: ActivityStream): Seq[StravaStream] = {
    activityStream.pivot map { row =>
      StravaStream.create(activity, row)
    }
  }

}

case class LapEffort(
  id: Long,
  resource_state: Int,
  name: String,
  elapsed_time: Int,
  moving_time: Int,
  start_date: String,
  start_date_local: String,
  distance: Float,
  start_index: Int,
  end_index: Int,
  total_elevation_gain: Float,
  average_speed: Float,
  max_speed: Float,
  average_cadence: Option[Float],
  average_watts: Float,
  device_watts: Option[Boolean],
  average_heartrate: Option[Float],
  max_heartrate: Option[Float],
  lap_index: Int
)

object LapEffort {
  implicit val reader: Reads[LapEffort] = Json.reads[LapEffort]
}

sealed trait Stream {
  val seriesType: String = "distance"
  val resolution: String = "high"
  val original_size: Int
  val data: Seq[Any]
}

case class EmptyStream(
  original_size: Int = 0,
  data: Seq[Any] = Seq.empty[Any]
) extends Stream

case class TimeStream(
  original_size: Int,
  data: Seq[Int] = Seq.empty
) extends Stream

case class LatLngStream(
  original_size: Int,
  data: Seq[Seq[Double]] = Seq.empty
) extends Stream

case class DistanceStream(
  original_size: Int,
  data: Seq[Double] = Seq.empty
) extends Stream

case class AltitudeStream(
  original_size: Int,
  data: Seq[Double] = Seq.empty
) extends Stream

case class VelocityStream(
  original_size: Int,
  data: Seq[Double] = Seq.empty
) extends Stream

case class HeartRateStream(
  original_size: Int,
  data: Seq[Int] = Seq.empty
) extends Stream

case class CadenceStream(
  original_size: Int,
  data: Seq[Int] = Seq.empty
) extends Stream

case class WattsStream(
  original_size: Int,
  data: Seq[Any] = Seq.empty
) extends Stream

case class TempStream(
  original_size: Int,
  data: Seq[Int] = Seq.empty
) extends Stream

case class MovingStream(
  original_size: Int,
  data: Seq[Boolean] = Seq.empty
) extends Stream

case class GradeStream(
  original_size: Int,
  data: Seq[Double] = Seq.empty
) extends Stream

object Stream {

  val intReader: Reads[Int] = Reads[Int](value => JsSuccess(value match {
    case n: JsNumber => n.value.toInt
    case JsNull => 0
    case _ => 0
  }))

  val seqOfIntReader = Reads.seq[Int](intReader)

  implicit val wattsReads: Reads[WattsStream] = (
    (JsPath \ "original_size").read[Int] and
      (JsPath \ "data").read(seqOfIntReader) // dang strava api sometimes puts 'null' in for value
    ) (WattsStream.apply _)


  implicit val timeFormat = Json.format[TimeStream]
  implicit val latLngFormat = Json.format[LatLngStream]
  implicit val distanceFormat = Json.format[DistanceStream]
  implicit val altitudeFormat = Json.format[AltitudeStream]
  implicit val velocityFormat = Json.format[VelocityStream]
  implicit val heartRateFormat = Json.format[HeartRateStream]
  implicit val cadenceFormat = Json.format[CadenceStream]
  implicit val tempFormat = Json.format[TempStream]
  implicit val movingFormat = Json.format[MovingStream]
  implicit val gradeFormat = Json.format[GradeStream]
}

case class ActivityStream(
  time: TimeStream,
  latLng: LatLngStream,
  distance: DistanceStream,
  altitude: AltitudeStream,
  velocity: VelocityStream,
  heartRate: HeartRateStream,
  cadence: CadenceStream,
  watts: WattsStream,
  temp: TempStream,
  moving: MovingStream,
  grade: GradeStream
) {

  def asMap = {
    val cc = this.asInstanceOf[Product]
    val streams = cc.productIterator map (_.asInstanceOf[Stream].data)
    cc.getClass.getDeclaredFields.map(_.getName -> streams.next()).toMap
  }

  def pivot = { // turns a list of stream "columns" into a list of stream "rows"
    val streamMap = this.asMap filter(_._2.nonEmpty) // remove any empty streams or else transpose will fail
    val streamTypes = streamMap.keys.toList
    val transposed = streamMap.values.toList.transpose
    transposed map (each => (streamTypes zip each).toMap)
  }

  def toInternalStreams(activity: StravaActivity): Seq[StravaStream] = {
    this.pivot map { row =>
      StravaStream.create(activity, row)
    }
  }

}

object ActivityStream {

  implicit val activityStreamReads: Reads[ActivityStream] = (
    (JsPath \ "time").read[TimeStream] and
      (JsPath \ "latlng").read[LatLngStream].orElse(Reads.pure(LatLngStream(0))) and
      (JsPath \ "distance").read[DistanceStream].orElse(Reads.pure(DistanceStream(0))) and
      (JsPath \ "altitude").read[AltitudeStream].orElse(Reads.pure(AltitudeStream(0))) and
      (JsPath \ "velocity_smooth").read[VelocityStream].orElse(Reads.pure(VelocityStream(0))) and
      (JsPath \ "heartrate").read[HeartRateStream].orElse(Reads.pure(HeartRateStream(0))) and
      (JsPath \ "cadence").read[CadenceStream].orElse(Reads.pure(CadenceStream(0))) and
      (JsPath \ "watts").read[WattsStream].orElse(Reads.pure(WattsStream(0))) and
      (JsPath \ "temp").read[TempStream].orElse(Reads.pure(TempStream(0))) and
      (JsPath \ "moving").read[MovingStream].orElse(Reads.pure(MovingStream(0))) and
      (JsPath \ "grade_smooth").read[GradeStream].orElse(Reads.pure(GradeStream(0)))
    ) (ActivityStream.apply _)

}

case class SummaryActivityMap(
  id: String,
  summary_polyline: Option[String]
)

case class SummaryActivityLocation(
  location_city: Option[String],
  location_state: Option[String],
  location_country: String,
  start_latitude: Option[Double],
  start_longitude: Option[Double]
)

case class SummaryActivityPerformance(
  average_speed: Double,
  max_speed: Double,
  average_cadence: Option[Double],
  average_watts: Option[Double],
  weighted_average_watts: Option[Int],
  kilojoules: Option[Double],
  device_watts: Option[Boolean],
  has_heartrate: Boolean,
  average_heartrate: Option[Double],
  max_heartrate: Option[Double],
  display_hide_heartrate_option: Boolean,
  max_watts: Option[Int]
)

case class SummaryActivitySocial(
  kudos_count: Int,
  comment_count: Int,
  athlete_count: Int,
  achievement_count: Int,
  photo_count: Int,
  total_photo_count: Int,
  has_kudoed: Boolean,
  pr_count: Int
)

case class SummaryActivityInfo(
//  athleteId: Int,
  name: String,
  distance: Double,
  moving_time: Int,
  elapsed_time: Int,
  total_elevation_gain: Double,
  `type`: String,
  id: Long,
  external_id: String,
  upload_id: Long,
  start_date: Instant,
  start_date_local: Instant,
  timezone: String,
  utc_offset: Int,
  trainer: Boolean,
  commute: Boolean,
  manual: Boolean,
  `private`: Boolean,
  visibility: String,
  flagged: Boolean,
  gear_id: Option[String]
)

case class SummaryActivity(
  info: SummaryActivityInfo,
  map: SummaryActivityMap,
  location: SummaryActivityLocation,
  performance: SummaryActivityPerformance,
  social: SummaryActivitySocial
)

object SummaryActivity {

  implicit val summaryActivityInfoReads: Reads[SummaryActivityInfo] = Json.reads[SummaryActivityInfo]

  implicit val summaryActivityMapReads: Reads[SummaryActivityMap] = Json.reads[SummaryActivityMap]

  implicit val summaryActivityLocationReads: Reads[SummaryActivityLocation] = Json.reads[SummaryActivityLocation]
  implicit val summaryActivityPerformanceReads: Reads[SummaryActivityPerformance] = Json.reads[SummaryActivityPerformance]
  implicit val summaryActivitySocialReads: Reads[SummaryActivitySocial] = Json.reads[SummaryActivitySocial]

  implicit val summaryActivityReads: Reads[SummaryActivity] = (
    JsPath.read[SummaryActivityInfo] and
      (JsPath \ "map").read[SummaryActivityMap] and
    JsPath.read[SummaryActivityLocation] and
    JsPath.read[SummaryActivityPerformance] and
    JsPath.read[SummaryActivitySocial]
  )(SummaryActivity.apply _)

}
