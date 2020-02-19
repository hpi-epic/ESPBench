package org.hpi.esb.datavalidator.metrics

import org.hpi.esb.datavalidator.data.Record

object CorrectnessMessages {

  def unequalValuesMsg(topicList: List[String], outputTopic: String, expectedValue: String, actualValue: String): String = {
    s"""
       |-------------------------
       |Topic $topicList; output topic/query: $outputTopic: The following two records differ:
       |Exp. $expectedValue
       |Act. $actualValue.
       |-------------------------""".stripMargin
  }

  def tooFewValueCreatedMsg(inputTopicList: List[String], outputTopic: String, valueType: String, valuePair: (Option[Record], Option[Record])): String =
    s"Too few $valueType were created in $outputTopic for input topic(s) $inputTopicList.\nValue expected in result topic: ${valuePair._1.orNull}"

  def tooManyValuedCreatedMsg(inputTopicList: List[String], outputTopic: String, valueType: String, valuePair: (Option[Record], Option[Record])): String =
    s"Too many $valueType were created in $outputTopic for input topic(s) $inputTopicList.\nValue not expected in result topic: ${valuePair._2.orNull}"
}

object Correctness {
  val header: List[String] = List("Correct")
}

class Correctness(private var isCorrect: Boolean = true) extends BenchmarkResult with ConstrainedMetric {

  def update(isCorrect: Boolean): Unit = {
    this.isCorrect &= isCorrect
  }

  override def getMeasuredResults: List[String] = List(fulfillsConstraint.toString)

  override def fulfillsConstraint: Boolean = isCorrect

  override def getResultsHeader: List[String] = Correctness.header
}
