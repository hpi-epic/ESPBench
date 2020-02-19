package org.hpi.esb.datasender.metrics

import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import org.hpi.esb.datasender.TestHelper.checkEquality


class SendMetricsTest extends FunSpec with MockitoSugar {

  val expectedRecordNumber = 1000
  val topicOffsets = Map("A" -> 10L, "B" -> 20L)
  val sendMetrics = new SendMetrics(topicOffsets, expectedRecordNumber)

  describe("getFailedSendsPercentage") {

    it("should return 0 when expectedRecordNumber = 0") {
      val failedSends = 100
      val expectedRecordNumber = 0
      val failedSendsResult = SendResult(expectedRecordNumber, failedSends)
      assert(0.0 == failedSendsResult.failedSendsPercentage())
    }

    it("should calculate correctly") {
      val failedSends = 10
      val expectedRecordNumber = 100
      val failedSendsResult = SendResult(expectedRecordNumber, failedSends)
      assert(0.1 == failedSendsResult.failedSendsPercentage())
    }
  }

  describe("getAccumulatedSendMetrics") {
    val perTopicSendMetrics = Map("A" -> SendResult(expectedRecordNumber = 100L, failedSends = 10L),
      "B" -> SendResult(expectedRecordNumber = 100L, failedSends = 5L))

    val expectedAccumulatedSendMetrics = Map(
      "expectedRecordNumber" -> 200L.toString,
      "failedSends" -> 15L.toString,
      "failedPercentage" -> 0.075.toString)

    val accumulatedSendMetrics = sendMetrics.getAccumulatedSendResults(perTopicSendMetrics)

    it("should calculate the correct overall stats") {

      checkEquality[String, String](expectedAccumulatedSendMetrics, accumulatedSendMetrics)
      checkEquality[String, String](accumulatedSendMetrics, expectedAccumulatedSendMetrics)
    }
  }
}
