package fixtures

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import models.{NewSettings, Settings, SettingsResultError, SettingsResultSuccess}

class SettingsFixture extends ProfileFixture {
  val newSettings = NewSettings(profileWithStrava.id, 147, 300, 200)
  val settingsId = UUID.randomUUID().toString
  val createdSettings = Settings(
    settingsId,
    newSettings.weight,
    newSettings.ftp,
    newSettings.maxHeartRate,
    Instant.now.truncatedTo(ChronoUnit.SECONDS)
  )

  val allSettings = for (n <- 1 to 3) yield createdSettings.copy(id = n.toString)

  val createdResult = SettingsResultSuccess(Seq(createdSettings))
  val listResult = SettingsResultSuccess(allSettings)
  val errorResult = SettingsResultError("this failed")
}
