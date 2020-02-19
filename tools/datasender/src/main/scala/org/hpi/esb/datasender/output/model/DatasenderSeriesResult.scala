package org.hpi.esb.datasender.output.model

import org.hpi.esb.commons.output.Util._
import org.hpi.esb.commons.output.model.SeriesResult

class DatasenderSeriesResult(l: List[DatasenderResultRow]) extends SeriesResult {

  def toTable(): List[List[String]] = {

    val resultValues = l.map(_.resultValues)

    val mergedResultValues = ResultValues(
      batchSizeAvg = average(resultValues.map(_.batchSizeAvg)),
      bufferAvailableBytes = average(resultValues.map(_.bufferAvailableBytes)),
      bufferExhaustedRate = average(resultValues.map(_.bufferExhaustedRate)),
      bufferPoolWaitRatio = average(resultValues.map(_.bufferPoolWaitRatio)),
      expectedRecordNumber = average(resultValues.map(_.expectedRecordNumber)),
      failedSends = average(resultValues.map(_.failedSends)),
      failedSendsPercentage = average(resultValues.map(_.failedSendsPercentage)),
      recordErrorRate = average(resultValues.map(_.recordErrorRate)),
      recordQueueTimeAvg = average(resultValues.map(_.recordQueueTimeAvg)),
      recordSendRate = average(resultValues.map(_.recordSendRate)),
      recordsPerRequestAvg = average(resultValues.map(_.recordsPerRequestAvg)),
      requestLatencyMax = average(resultValues.map(_.requestLatencyMax)),
      waitingThreads = average(resultValues.map(_.waitingThreads))
    )

    val configValues = l.head.configValues
    val header: List[String] = ConfigValues.header ++ ResultValues.header
    List(header, configValues.toList ++ mergedResultValues.toList())
  }
}
