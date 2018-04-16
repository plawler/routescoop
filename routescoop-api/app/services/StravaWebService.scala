package services

import javax.inject.Inject

import com.google.inject.Singleton
import com.typesafe.scalalogging.LazyLogging
import kiambogo.scrava.ScravaClient
import models._
import modules.NonBlockingContext
import play.api.libs.ws.WSClient
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal


trait StravaWebService {

  def getActivities(userId: String): Future[Seq[StravaActivity]]

  def getLaps(activity: StravaActivity): Future[Seq[StravaLap]]

  def getStreams(activity: StravaActivity): Future[Seq[StravaStream]]

}

@Singleton
class StravaWebServiceImpl @Inject()(userService: UserService, ws: WSClient)(implicit @NonBlockingContext ec: ExecutionContext)
  extends StravaWebService with LazyLogging {

  override def getActivities(userId: String): Future[Seq[StravaActivity]] = {
    getUser(userId).map {
      case Some(user) =>
        logger.info("User exists. Fetching activities from Strava...")
        getUserActivities(user)
      case None => Nil
    }
  }

  override def getLaps(activity: StravaActivity): Future[Seq[StravaLap]] = {
    getUser(activity.userId) map {
      case Some(user) =>
        user.stravaToken match {
          case Some(token) =>
            createClient(token).listActivityLaps(activity.stravaId).map(lap => StravaLap.create(activity, lap))
          case None => Nil
        }
      case None => Nil
    }
  }

  def getStreams(activity: StravaActivity): Future[Seq[StravaStream]] = {
    val keys = "time,latlng,distance,altitude,velocity_smooth,heartrate,cadence,watts,temp,moving,grade_smooth"
    val url = s"https://www.strava.com/api/v3/activities/${activity.stravaId}/streams?keys=$keys&key_by_type=true"
    getToken(activity.userId) flatMap {
      case Some(token) =>
        ws.url(url).withHeaders("Authorization" -> s"Bearer $token").get() map { response =>
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

  private def getUserActivities(user: User): Seq[StravaActivity] = {
    user.stravaToken match {
      case Some(token) =>
        val activities = createClient(token).listAthleteActivities()
        logger.info(s"Found ${activities.size} athlete activities")
        activities.map(summary => StravaActivity.create(user, summary))
      case None =>
        logger.warn(s"User ${user.id} has no Strava token")
        Nil
    }
  }

  private def createClient(token: String) = new ScravaClient(token)

  private def getScravaClient(userId: String): Future[Option[ScravaClient]] = {
    getUser(userId) map {
      case Some(user) =>
        user.stravaToken match {
          case Some(token) => Some(createClient(token))
          case _ => None
        }
      case _ => None
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
