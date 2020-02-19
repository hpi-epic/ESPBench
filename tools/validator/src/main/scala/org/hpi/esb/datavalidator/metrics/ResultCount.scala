package org.hpi.esb.datavalidator.metrics

import org.hpi.esb.datavalidator.metrics.CountType.CountType

object ResultCount {
  val header: List[String] = List("Input", "Expected Output", "Actual Output", "False Output", "Correctness %")
}

object CountType extends Enumeration {
  type CountType = Value
  val Error, Actual, Expected = Value
}

class ResultCount(val inputSize: Long) extends BenchmarkResult {

  var actual, expected, errors: Int = 0

  def increment(countType: CountType): Unit = {
    countType match {
      case CountType.Error => errors += 1
      case CountType.Actual => actual += 1
      case CountType.Expected => expected += 1
    }
  }

  def computeCorrectness(): Double = {
    def round(number: Float): Double = BigDecimal(number.toDouble).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

    val offset = 2 * math.max(actual - expected, 0)
    val divisor = math.max(expected.toFloat, 1)
    val correctness: Double = if (actual == expected && errors == 0) {
      100
    } else {
      round(math.max(actual - (errors + offset), 0) / divisor * 100)
    }
    correctness
  }

  override def getMeasuredResults: List[String] = {
    val correctness = computeCorrectness()
    List(inputSize.toString, expected.toString, actual.toString, errors.toString, correctness.toString)
  }

  override def getResultsHeader: List[String] = ResultCount.header
}
