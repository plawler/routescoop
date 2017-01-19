package services

import kiambogo.scrava.ScravaClient
import kiambogo.scrava.models.PersonalActivitySummary
import models.{StravaActivity, User}
import repositories.StravaActivityStore

import com.fasterxml.uuid.Generators
import com.google.inject.Singleton

import java.time.Instant
import javax.inject.Inject


trait StravaWebService {

  def getLatestActivities(user: User): Seq[StravaActivity] // make future?

}

@Singleton
class ScravaWebService @Inject()(activityStore: StravaActivityStore) extends StravaWebService {

  override def getLatestActivities(user: User): Seq[StravaActivity] = {
    user.stravaToken match {
      case Some(token) =>
        val activities = getClient(token).listAthleteActivities()
        filterLatest(user.id, activities).map(summary => toActivity(user, summary))
      case None => Nil
    }
  }

  private def getClient(token: String) = new ScravaClient(token)

  private def filterLatest(
    userId: String,
    activities: List[PersonalActivitySummary]
  ): List[PersonalActivitySummary] = {
    val filtered = activities.filterNot(a => activityStore.findByUserId(userId).exists(a.id == _.stravaId))
    filtered
  }

  private def toActivity(user: User, summary: PersonalActivitySummary): StravaActivity = {
    StravaActivity(
      Generators.randomBasedGenerator().generate().toString,
      user.id,
      stravaId = summary.id,
      user.stravaId.get,
      summary.name,
      summary.distance,
      summary.moving_time,
      summary.elapsed_time,
      summary.total_elevation_gain,
      summary.`type`,
      Instant.parse(summary.start_date),
      summary.timezone,
      summary.start_latlng.head,
      summary.start_latlng(1),
      summary.trainer,
      summary.commute,
      summary.manual,
      summary.average_speed,
      summary.max_speed,
      summary.external_id,
      summary.end_latlng.map(_.head.toDouble),
      summary.end_latlng.map(_ (1).toDouble),
      summary.map.summary_polyline,
      summary.map.polyline,
      summary.average_cadence.map(_.toDouble),
      summary.average_temp,
      summary.average_watts.map(_.toDouble),
      weightedAverageWatts = None,
      summary.kilojoules.map(_.toDouble),
      summary.device_watts,
      summary.average_heartrate.map(_.toDouble),
      summary.max_heartrate.map(_.toDouble),
      summary.workout_type
    )
  }

}
