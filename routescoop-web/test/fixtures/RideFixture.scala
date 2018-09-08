package fixtures

import java.time.Instant
import java.util.UUID

import models.RideSync

trait RideFixture extends ProfileFixture {
  val rideSync = RideSync(UUID.randomUUID().toString, profileWithStrava.id, Instant.now)
}
