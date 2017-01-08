package fixtures

import models.User

/**
  * Created by paullawler on 1/5/17.
  */
trait UserFixture {

  val user = User("theUserId", "Joe User", "joe@user.com")
  val stravaUser = user.copy(id = "theStravaUserId", stravaId = Some(123456), stravaToken = Some("theStravaToken"))

}
