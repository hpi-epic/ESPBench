package org.hpi.esb.datavalidator.output.writers

import org.hpi.esb.commons.output.writers.ResultWriter
import org.hpi.esb.datavalidator.output.model.ValidatorResultRow
import org.scalatest.FunSpec

import scala.io.Source

class ValidatorSeriesResultWriterTest extends FunSpec {

  val writer = new ValidatorSeriesResultWriter(
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
    assert(resultTable(0) == header)
    assert(resultTable(1) == expectedResultRow)
  }

  describe("getFinalResult") {
    val header = ValidatorResultRow.header
    val correctnesses = ("50.0", "50.0", "50.0")

    it("should return 'correct = true' and 'RT-Fulfilled = true' when all constraints are passed") {
      val valuesToTest = List(
        // (row1, row2, expected)
        ("100.0", "100.0", "100.0"), // sendingInterval
        ("NANOSECONDS", "NANOSECONDS", "NANOSECONDS"), // sendingIntervalTimeUnit

        ("Identity", "Identity", "Identity"), // Query
        ("true", "true", "true"), // Correct
        ("1500.0", "1900.0", "1700.0"), // RT-90%ile
        ("true", "true", "true"), // RT-Fulfilled
        ("10.0", "20.0", "15.0"), // ValidatorRuntime
        correctnesses
      )
      testValues(writer, header, valuesToTest)
    }

    it("should return 'correct = false' when one of the results is false") {
      val valuesToTest = List(
        // (row1, row2, expected)
        ("100.0", "100.0", "100.0"), // sendingInterval
        ("NANOSECONDS", "NANOSECONDS", "NANOSECONDS"), // sendingIntervalTimeUnit

        ("Identity", "Identity", "Identity"), // Query
        ("false", "true", "false"), // Correct
        ("1500.0", "1900.0", "1700.0"), // RT-90%ile
        ("true", "true", "true"), // RT-Fulfilled
        ("10.0", "20.0", "15.0"), // ValidatorRuntime
        correctnesses
      )
      testValues(writer, header, valuesToTest)
    }

    it("should return 'RT-Fulfilled = false' when average RT-90%ile not fulfilled") {
      val valuesToTest = List(
        // (row1, row2, expected)
        ("100.0", "100.0", "100.0"), // sendingInterval
        ("NANOSECONDS", "NANOSECONDS", "NANOSECONDS"), // sendingIntervalTimeUnit

        ("Identity", "Identity", "Identity"), // Query
        ("true", "true", "true"), // Correct
        ("1900", "2500.0", "2200.0"), // RT-90%ile
        ("true", "false", "false"), // RT-Fulfilled
        ("10.0", "20.0", "15.0"), // ValidatorRuntime
        correctnesses
      )
      testValues(writer, header, valuesToTest)
    }
  }
}
