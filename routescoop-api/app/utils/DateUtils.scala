package utils

import java.time.Instant
import java.time.temporal.ChronoUnit

object DateUtils {
  def now: Instant = Instant.now.truncatedTo(ChronoUnit.MICROS)
}
