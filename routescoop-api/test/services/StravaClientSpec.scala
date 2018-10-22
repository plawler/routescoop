package services

import fixtures.UserFixture
import kiambogo.scrava.ScravaClient
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by paullawle r on 12/6/16.
  */
class StravaClientSpec extends FlatSpec with Matchers {

  "The StravaService" should "initialize" in new TestAthleteFixture {}

  it should "retrieve the laps for an activity" in new TestAthleteFixture {
    val activity = client.listAthleteActivities().head
    val laps = client.listActivityLaps(activity.id)
    laps shouldNot be(empty)
  }

  def flatten(ls: List[Any]): List[Any] = ls flatMap {
    case i: List[_] => flatten(i)
    case e => List(e)
  }

  trait TestAthleteFixture extends UserFixture {
    val client = new ScravaClient(stravaUser.stravaToken.get)
  }

  trait PaulAthleteFixture extends UserFixture {
    val client = new ScravaClient(paul.stravaToken.get)
  }

}
