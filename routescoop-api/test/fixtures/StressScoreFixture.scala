package fixtures

import models.DailyStress

import java.time.LocalDate

trait StressScoreFixture {
  val stressScores = Seq(83, 0, 109, 77, 138, 0, 82, 99, 48, 0, 0)
  val startingCtl = 52.0
  val startingAtl = 65.0
  val startDate = LocalDate.of(2018, 11, 21)
  val stresses = stressScores.zipWithIndex.map(t => DailyStress(startDate.plusDays(t._2), t._1))

  val stressesWithWeek = Seq(
    DailyStress(LocalDate.of(2018,11,18), 162, 46),
    DailyStress(LocalDate.of(2018,11,19), 0, 46),
    DailyStress(LocalDate.of(2018,11,20),	73,	46),
    DailyStress(LocalDate.of(2018,11,21),	74,	46),
    DailyStress(LocalDate.of(2018,11,22),	0, 46),
    DailyStress(LocalDate.of(2018,11,23),	103, 46),
    DailyStress(LocalDate.of(2018,11,24),	78,	46),
    DailyStress(LocalDate.of(2018,11,25),	123, 47),
    DailyStress(LocalDate.of(2018,11,26),	0, 47),
    DailyStress(LocalDate.of(2018,11,27),	82,	47),
    DailyStress(LocalDate.of(2018,11,28),	98,	47),
    DailyStress(LocalDate.of(2018,11,29),	49,	47),
    DailyStress(LocalDate.of(2018,11,30),	0, 47),
    DailyStress(LocalDate.of(2018,12,1), 0, 47)
  )
}
