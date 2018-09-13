package fixtures

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import models.{NewSettings, Settings}

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
}
