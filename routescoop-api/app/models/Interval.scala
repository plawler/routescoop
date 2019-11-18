package models

case class Interval(
  lengthInSeconds: Int,
  startSecond: Int, // the second in the stream that calculated maximum effort was found
  criticalPower: Int,
  avgHeartRate: Int,
  normalizedPower: Option[Int]
)
