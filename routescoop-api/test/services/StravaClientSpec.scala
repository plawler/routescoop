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
    activities.size shouldEqual 5
  }

  it should "retrieve the laps for an activity" in new Fixture {
    val activity = client.listAthleteActivities().head
    val laps = client.listActivityLaps(activity.id)
    laps shouldNot be(empty)
  }

  it should "retrieve an activity stream" in new Fixture {
    val activities = client.listAthleteActivities()
    val activity = activities.head
    val activity2 = client.retrieveActivity(activity.id)
    val streams = client.retrieveActivityStream(activity.id.toString)
    // List(latlng, time, distance, altitude, heartrate, cadence, watts, temp, grade_smooth, moving, velocity_smooth)
    //    streams.foreach(s => println(s.data.size))
    //    println(streams(4).data.take(10))
    //    println(streams(6).data.take(10))
    streams shouldNot be(empty)
  }

  trait Fixture extends UserFixture {
    val client = new ScravaClient(stravaUser.stravaToken.get)
  }

}
