package org.hpi.esb.commons.output

object Util {

  def round(value: Double, precision: Int): Double = {
    val base = 10
    val v = math.pow(base, precision)
    math.round(value * v) / v
  }

  def roundPrecise(value: BigDecimal, precision: Int): Double = {
    value.setScale(precision, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def format(value: Double): String = {
    f"$value%1.1f"
  }

  def average[T](ts: Iterable[T])(implicit num: Numeric[T]) = {
    num.toDouble(ts.sum) / ts.size
  }
}
