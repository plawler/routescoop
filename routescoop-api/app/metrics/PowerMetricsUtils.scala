package metrics

case class Result(index: Int, value: Int)

object PowerMetricsUtils {

  // inspired by https://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list

  /**
    * Function which calculates the Mean Maximum value for a given interval. The interval can also be thought of as a
    * window size slid across the values calculating an average and then taking the largest average.
    *
    * @param values
    * @param interval
    * @return the maximum average value for the interval
    */
  def maxAverage(values: Seq[Int], interval: Int): Int = {
    rollingAverage(values, interval).max
  }

  /**
    * Function which calculates the Mean Maximum value for a given interval. The interval can also be thought of as a
    * window size slid across the values calculating an average and then taking the largest average.
    *
    * @param values
    * @param interval
    * @return a tuple containing the maximum average value with its index
    */
  def maxAverageWithIndex(values: Seq[Int], interval: Int): (Int, Int) = {
    rollingAverage(values, interval).zipWithIndex.maxBy(_._1)
  }

  /**
    * Calculates the normalized power function for a series of values
    *
    * Normalized power is as follows -
    * 1. Calculate a 30-second rolling average of the power data
    * 2. Raise these values to the fourth power
    * 3. Average the resulting values
    * 4. Take the fourth root of the result
    *
    * @param values
    * @return
    */
  def normalizedPower(values: Seq[Int]): Option[Int] = {
    if (values.length < 30) {
      None
    } else {
      val thirties = rollingAverage(values, 30)
      val raised = thirties.map(n => scala.math.pow(n, 4))
      val avg = raised.sum / raised.size
      Some(scala.math.pow(avg, 1.0 / 4).toInt)
    }
  }

  def rollingAverage(values: Seq[Int], interval: Int): Seq[Int] = {
    if (interval == 0) throw new IllegalArgumentException("Cannot calculate with zero interval")
    val first = (values take interval).sum / interval
    val subtract = values map (_ / interval)
    val add = subtract drop interval
    val addAndSubtract = add zip subtract map (as => as._1 - as._2)

    addAndSubtract.foldLeft(Seq(first)) { (acc, add) =>
      (add + acc.head) +: acc
    }.reverse
  }

  //  def rollingAveragePadded(values: Seq[Int], interval: Int): Seq[Int] = {
  //    val first = (values take interval).sum / interval
  //    val subtract = values map (_ / interval)
  //    val add = subtract drop interval
  //    val addAndSubtract = add zip subtract map (as => as._1 - as._2)
  //
  //    addAndSubtract.foldLeft(first :: List.fill(interval - 1)(0)) { (acc, add) =>
  //      (add + acc.head) +: acc
  //    }.reverse
  //  }


}
