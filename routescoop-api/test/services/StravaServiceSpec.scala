package services

import kiambogo.scrava.ScravaClient
import models.User
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by paullawle r on 12/6/16.
  */
class StravaServiceSpec extends FlatSpec with Matchers {

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
    val activity = client.listAthleteActivities().head
    val streams = client.retrieveActivityStream(activity.id.toString)
    // List(latlng, time, distance, altitude, heartrate, cadence, watts, temp, grade_smooth, moving, velocity_smooth)
    //    streams.foreach(s => println(s.data.size))
    //    println(streams(4).data.take(10))
    //    println(streams(6).data.take(10))
    streams shouldNot be(empty)
  }

  trait Fixture {

    val user = User(
      "582efc4a761e61454c13410d",
      "paul.lawler+routescooptest1",
      "paul.lawler+routescooptest1@gmail.com",
      Some("https://s.gravatar.com/avatar/d8dc4888794e249ba39d0510d76a8b01?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fpa.png"),
      Some("2887c991b898b497bff9d9c180a3a66ea579624b"),
      Some(18629225)
    )

    val client = new ScravaClient(user.stravaToken.get)
  }

}
