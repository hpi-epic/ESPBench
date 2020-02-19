package org.hpi.esb.datasender.metrics

import org.hpi.esb.commons.output.Util._
import org.hpi.esb.util.OffsetManagement

case class SendResult(expectedRecordNumber: Long = 0, failedSends: Long = 0) {
  def failedSendsPercentage(): Double = {
    val failedPercentage = if (expectedRecordNumber != 0) {
      BigDecimal(failedSends) / BigDecimal(expectedRecordNumber)
    } else {
      BigDecimal(0)
    }
    roundPrecise(failedPercentage, precision = 3)
  }

  def update(expectedRecordNumber: Long, failedSends: Long): SendResult = {
    val sumExpectedRecordNumber = this.expectedRecordNumber + expectedRecordNumber
    val sumFailedSends = this.failedSends + failedSends
    SendResult(sumExpectedRecordNumber, sumFailedSends)
  }
}

class SendMetrics(topicStartOffsets: Map[String, Long], expectedTopicNumber: Long) extends Metric {

  val expectedRecordNumberName = "expectedRecordNumber"
  val failedSendsName = "failedSends"
  val failedPercentageName = "failedPercentage"

  override def getMetrics(): Map[String, String] = {
    val sendResults = topicStartOffsets.map { case (topic, startOffset) =>
      topic -> getSendResultForTopic(topic, startOffset, expectedTopicNumber)
    }

    getAccumulatedSendResults(sendResults)
  }

  def getSendResultForTopic(topic: String, offset: Long, expectedRecordNumber: Long): SendResult = {
    val latestOffset = OffsetManagement.getNumberOfMessages(topic, partition = 0)
    val actualRecordNumber = latestOffset - offset
    val failedSends = expectedRecordNumber - actualRecordNumber
    SendResult(expectedRecordNumber = expectedRecordNumber, failedSends = failedSends)
  }

  def getAccumulatedSendResults(sendMetrics: Map[String, SendResult]): Map[String, String] = {

    val overallSendResults = sendMetrics.foldLeft(SendResult()) {
      case (acc, (_, SendResult(expectedRecordNumber, failedSends))) => acc.update(expectedRecordNumber, failedSends) }

    Map(expectedRecordNumberName -> overallSendResults.expectedRecordNumber.toString,
      failedSendsName -> overallSendResults.failedSends.toString,
      failedPercentageName -> overallSendResults.failedSendsPercentage().toString)
  }
}
