package fixtures

import java.time.temporal.ChronoUnit

import models.{User, UserSettings}

/**
  * Created by paullawler on 1/5/17.
  */
trait UserFixture {

  val user = User("theUserId", "Joe User", "joe@user.com")

  val stravaUser = User(
    "582efc4a761e61454c13410d",
    "paul.lawler+routescooptest1",
    "paul.lawler+routescooptest1@gmail.com",
    Some("https://s.gravatar.com/avatar/d8dc4888794e249ba39d0510d76a8b01?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fpa.png"),
    Some("2887c991b898b497bff9d9c180a3a66ea579624b"),
    Some(18629225)
  )

  val userSettings = UserSettings("theSettingsId", user.id, 155, 285, 200)
  val yearOldSettings = userSettings.copy(createdAt = userSettings.createdAt.minus(365, ChronoUnit.DAYS))
  val twoYearOldSettings = userSettings.copy(createdAt = userSettings.createdAt.minus(730, ChronoUnit.DAYS))

  val allSettings = Seq(userSettings, yearOldSettings, twoYearOldSettings)

  val paul = User(
    "5b8dba213298951dabb098f5",
    "paul.lawler",
    "paul.lawler@gmail.com",
    None,
    Some("5bdcec198a22eab01151c947e6d99ebf250ee4de"),
    Some(18629225)
  )

}
