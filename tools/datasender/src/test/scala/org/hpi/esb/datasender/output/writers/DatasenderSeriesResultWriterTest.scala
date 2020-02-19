package org.hpi.esb.datasender.output.writers

import org.hpi.esb.commons.output.writers.ResultWriter
import org.hpi.esb.datasender.output.model.DatasenderResultRow
import org.scalatest.FunSpec

import scala.io.Source

class DatasenderSeriesResultWriterTest extends FunSpec {

  val writer = new DatasenderSeriesResultWriter(
    inputFilesPrefix = "Run_",
    resultsPath = "/OutputPath/",
    outputFileName = "OutputFileName.csv")

  def testValues(writer: ResultWriter, header: List[String], valuesToTest: List[(String, String, String)]): Unit = {
    val (row1, row2, expectedResultRow) = valuesToTest.unzip3
    val csvHeader = header.mkString(",")
    val comma = ","
    val sources = List(Source.fromString(s"$csvHeader\n${row1.mkString(comma)}"),
      Source.fromString(s"$csvHeader\n${row2.mkString(comma)}"))

    val resultTable = writer.createResultTable(sources)
    assert(resultTable.length == 2)
    assert(resultTable.head == header)
    assert(resultTable(1) == expectedResultRow)
  }

  describe("DatasenderSeriesResultWriter") {

    it("should getFinalResult") {

      val header = DatasenderResultRow.header

      val valuesToTest = List(
        // (row1, row2, expected)
        ("100.0", "100.0", "100.0"), // batchSize
        ("100.0", "100.0", "100.0"), // bufferMemorySize
        ("100.0", "100.0", "100.0"), // lingerTime
        ("false", "false", "false"), // readInRam
        ("100.0", "100.0", "100.0"), // sendingInterval
        ("NANOSECONDS", "NANOSECONDS", "NANOSECONDS"), // sendingIntervalTimeUnit

        ("100.0", "200.1", "150.1"), // batch-size-avg
        ("100.0", "200.1", "150.1"), // buffer-available-bytes
        ("100.0", "200.1", "150.1"), // buffer-exhausted-rate"
        ("100.1", "200.1", "150.1"), // bufferpool-wait-ratio"
        ("100.0", "200.0", "150.0"), // "expectedRecordNumber"
        ("10.0", "20.0", "15.0"), // "failedSends"
        ("10.0", "10.0", "10.0"), // "failedPercentage"
        ("100.1", "200.1", "150.1"), // "record-error-rate"
        ("100.1", "200.1", "150.1"), // record-queue-time-avg
        ("100.1", "200.1", "150.1"), // record-send-rate
        ("100.1", "200.1", "150.1"), // records-per-request-avg
        ("100.1", "200.1", "150.1"), // request-latency-max
        ("100.1", "200.1", "150.1") // waiting-threads
      )
      testValues(writer, header, valuesToTest)
    }
  }
}
