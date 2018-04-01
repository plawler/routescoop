package services

import fixtures.UserFixture
import kiambogo.scrava.ScravaClient
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by paullawle r on 12/6/16.
  */
class StravaClientSpec extends FlatSpec with Matchers {

  "The StravaService" should "initialize" in new Fixture {}

  it should "retieve an athlete" in new Fixture {
    val athlete = client.retrieveAthlete() // result will be left biased, no athlete id
    athlete.left.map(_.lastname shouldEqual "McTester")
  }

  it should "retrieve an athlete's activities" in new Fixture {
    val activities = client.listAthleteActivities()
    activities should not be empty
  }

  it should "retrieve the laps for an activity" in new Fixture {
    val activity = client.listAthleteActivities().head
    val laps = client.listActivityLaps(activity.id)
    laps shouldNot be(empty)
  }

  it should "retrieve an activity stream" in new Fixture {
    val activities = client.listAthleteActivities()
    val activity = activities.head
//    println(activity.id)
    val activity2 = client.retrieveActivity(activity.id)
    val streams = client.retrieveActivityStream(activity.id.toString)
    streams shouldNot be(empty)

//    val types = streams.map(_.`type`)
//    val streamData = streams map (_.data)
//    val pivoted = streamData.transpose
//    val hashed = pivoted.take(5) map (ss => (types zip ss).toMap)
//
//    val stravaStreams = hashed map (raw => StravaStream.create(StravaActivity.create(stravaUser, activity), raw))
//
//    val smushed = pivoted.take(5) map { s =>
//      flatten(s)
//    }
//    println(smushed)
  }

  def flatten(ls: List[Any]): List[Any] = ls flatMap {
    case i: List[_] => flatten(i)
    case e => List(e)
  }


  trait Fixture extends UserFixture {
    val client = new ScravaClient(stravaUser.stravaToken.get)
  }

}
