package org.hpi.esb.datasender.output.model

import org.scalatest.FunSpec
import ResultValues._

class ResultValuesTest extends FunSpec {

  val batchSizeAvg = "100.0"
  val bufferAvailableBytes = "100.0"
  val bufferExhaustedRate = "100.0"
  val bufferPoolWaitRatio = "100.0"
  val expectedRecordNumber = "100.0"
  val failedSends = "100.0"
  val failedSendsPercentage = "100.0"
  val recordErrorRate = "100.0"
  val recordQueueTimeAvg = "100.0"
  val recordSendRate = "100.0"
  val recordsPerRequestAvg = "100.0"
  val requestLatencyMax = "100.0"
  val waitingThreads = "100.0"

  val exampleResultValues = ResultValues(
    batchSizeAvg = batchSizeAvg.toDouble,
    bufferAvailableBytes = bufferAvailableBytes.toDouble,
    bufferExhaustedRate = bufferExhaustedRate.toDouble,
    bufferPoolWaitRatio = bufferPoolWaitRatio.toDouble,
    expectedRecordNumber = expectedRecordNumber.toDouble,
    failedSends = failedSends.toDouble,
    failedSendsPercentage = failedSendsPercentage.toDouble,
    recordErrorRate = recordErrorRate.toDouble,
    recordQueueTimeAvg = recordQueueTimeAvg.toDouble,
    recordSendRate = recordSendRate.toDouble,
    recordsPerRequestAvg = recordsPerRequestAvg.toDouble,
    requestLatencyMax = requestLatencyMax.toDouble,
    waitingThreads = waitingThreads.toDouble)

  describe("toList") {
    it("should return a list representation of the result values") {
      val exampleResultValuesList = exampleResultValues.toList()
      val expectedList = List(batchSizeAvg, bufferAvailableBytes, bufferExhaustedRate,
        bufferPoolWaitRatio, expectedRecordNumber, failedSends, failedSendsPercentage,
        recordErrorRate, recordQueueTimeAvg, recordSendRate, recordsPerRequestAvg,
        requestLatencyMax, waitingThreads)

      assert(exampleResultValuesList == expectedList)
    }
  }

  describe("fromMap") {
    it("should return a ResultValues object") {
      val valueMap = Map(
        BATCH_SIZE_AVERAGE -> batchSizeAvg,
        BUFFER_AVAILABLE_BYTES -> bufferAvailableBytes,
        BUFFER_EXHAUSTED_RATE -> bufferExhaustedRate,
        BUFFERPOOL_WAIT_RATIO -> bufferPoolWaitRatio,
        EXPECTED_RECORD_NUMBER -> expectedRecordNumber,
        FAILED_SENDS -> failedSends,
        FAILED_SEND_PERCENTAGE -> failedSendsPercentage,
        RECORD_ERROR_RATE -> recordErrorRate,
        RECORD_QUEUE_TIME_AVG -> recordQueueTimeAvg,
        RECORD_SEND_RATE -> recordSendRate,
        RECORDS_PER_REQUEST_AVG -> recordsPerRequestAvg,
        REQUEST_LATENCY_MAX -> requestLatencyMax,
        WAITING_THREADS -> waitingThreads
      )
      val resultValuesFromMap = new ResultValues(valueMap)

      assert(resultValuesFromMap == exampleResultValues)
    }
  }
}
