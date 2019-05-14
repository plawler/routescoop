package utils

import java.text.DecimalFormat

object NumberUtils {

  def decimalFormat(d: Double, pattern: String = "#.#") = new DecimalFormat(pattern).format(d)

  def roundUp(d: Double, scale: Int): Double = BigDecimal(d).setScale(scale, BigDecimal.RoundingMode.UP).toDouble
  def truncate(d: Double, scale: Int): Double = BigDecimal(d).setScale(scale, BigDecimal.RoundingMode.DOWN).toDouble

}
