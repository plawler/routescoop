package fixtures

import models.Profile

trait ProfileFixture extends SessionFixture {
  val profileKey = sessionId + "profile"
  val id = "e6ef344c-3220-4522-9210-f76c1a8e0b49"
  val name = "Bob"
  val email = "bob@strava.com"
  val pic = ""
  val profile = Profile(id, name, email, pic)
  val profileWithStrava = profile.copy(stravaId = Some(1234567890), stravaToken = Some("theStravaToken"))
}

