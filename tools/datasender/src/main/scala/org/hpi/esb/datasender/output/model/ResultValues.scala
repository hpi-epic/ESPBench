package org.hpi.esb.datasender.output.model

import org.hpi.esb.commons.output.Util._

object ResultValues {
  val BATCH_SIZE_AVERAGE = "batch-size-avg"
  val BUFFER_AVAILABLE_BYTES = "buffer-available-bytes"
  val BUFFER_EXHAUSTED_RATE = "buffer-exhausted-rate"
  val BUFFERPOOL_WAIT_RATIO = "bufferpool-wait-ratio"
  val EXPECTED_RECORD_NUMBER = "expectedRecordNumber"
  val FAILED_SENDS = "failedSends"
  val FAILED_SEND_PERCENTAGE = "failedPercentage"
  val RECORD_ERROR_RATE = "record-error-rate"
  val RECORD_QUEUE_TIME_AVG = "record-queue-time-avg"
  val RECORD_SEND_RATE = "record-send-rate"
  val RECORDS_PER_REQUEST_AVG = "records-per-request-avg"
  val REQUEST_LATENCY_MAX = "request-latency-max"
  val WAITING_THREADS = "waiting-threads"

  val header = List(BATCH_SIZE_AVERAGE, BUFFER_AVAILABLE_BYTES, BUFFER_EXHAUSTED_RATE,
    BUFFERPOOL_WAIT_RATIO, EXPECTED_RECORD_NUMBER, FAILED_SENDS, FAILED_SEND_PERCENTAGE,
    RECORD_ERROR_RATE, RECORD_QUEUE_TIME_AVG, RECORD_SEND_RATE, RECORDS_PER_REQUEST_AVG,
    REQUEST_LATENCY_MAX, WAITING_THREADS)

}

import org.hpi.esb.datasender.output.model.ResultValues._

case class ResultValues(batchSizeAvg: Double, bufferAvailableBytes: Double, bufferExhaustedRate: Double,
                        bufferPoolWaitRatio: Double, expectedRecordNumber: Double, failedSends: Double,
                        failedSendsPercentage: Double, recordErrorRate: Double, recordQueueTimeAvg: Double,
                        recordSendRate: Double, recordsPerRequestAvg: Double, requestLatencyMax: Double,
                        waitingThreads: Double) {

  def this(m: Map[String, String]) = this(m(BATCH_SIZE_AVERAGE).toDouble, m(BUFFER_AVAILABLE_BYTES).toDouble,
    m(BUFFER_EXHAUSTED_RATE).toDouble, m(BUFFERPOOL_WAIT_RATIO).toDouble, m(EXPECTED_RECORD_NUMBER).toDouble,
    m(FAILED_SENDS).toDouble, m(FAILED_SEND_PERCENTAGE).toDouble, m(RECORD_ERROR_RATE).toDouble,
    m(RECORD_QUEUE_TIME_AVG).toDouble, m(RECORD_SEND_RATE).toDouble, m(RECORDS_PER_REQUEST_AVG).toDouble,
    m(REQUEST_LATENCY_MAX).toDouble, m(WAITING_THREADS).toDouble)

  def roundAndFormat(value: Double): String = {
    format(round(value, precision = 2))
  }
  def toList(): List[String] = {
    val fBatchSizeAvg = roundAndFormat(batchSizeAvg)
    val fBufferAvailableBytes = roundAndFormat(bufferAvailableBytes)
    val fBufferExhaustedRate = roundAndFormat(bufferExhaustedRate)
    val fBufferPoolWaitRatio = roundAndFormat(bufferPoolWaitRatio)
    val fExpectedRecordNumber = roundAndFormat(expectedRecordNumber)
    val fFailedSends = roundAndFormat(failedSends)
    val fFailedSendsPercentage = roundAndFormat(failedSendsPercentage)
    val fRecordErrorRate = roundAndFormat(recordErrorRate)
    val fRecordQueueTimeAvg = roundAndFormat(recordQueueTimeAvg)
    val fRecordSendRate = roundAndFormat(recordSendRate)
    val fRecordsPerRequestAvg = roundAndFormat(recordsPerRequestAvg)
    val fRequestLatencyMax = roundAndFormat(requestLatencyMax)
    val fWaitingThreads = roundAndFormat(waitingThreads)

    List(fBatchSizeAvg, fBufferAvailableBytes, fBufferExhaustedRate,
      fBufferPoolWaitRatio, fExpectedRecordNumber, fFailedSends,
      fFailedSendsPercentage, fRecordErrorRate, fRecordQueueTimeAvg,
      fRecordSendRate, fRecordsPerRequestAvg, fRequestLatencyMax, fWaitingThreads)
  }
}

