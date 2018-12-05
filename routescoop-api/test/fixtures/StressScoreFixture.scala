package fixtures

import models.DailyStress

import java.time.LocalDate

trait StressScoreFixture {
  val stressScores = Seq(83, 0, 109, 77, 138, 0, 82, 99, 48, 0, 0)
  val startingCtl = 52.0
  val startingAtl = 65.0
  val startDate = LocalDate.of(2018, 11, 21)
  val stresses = stressScores.zipWithIndex.map(t => DailyStress(startDate.plusDays(t._2), t._1))
}
