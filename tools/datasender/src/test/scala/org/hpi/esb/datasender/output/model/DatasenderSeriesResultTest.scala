package org.hpi.esb.datasender.output.model

import org.scalatest.FunSpec

class DatasenderSeriesResultTest extends FunSpec {

  describe("toTable") {

    it("should return a table representing the series result") {
      val configValues = ConfigValues(
        batchSize = "1000",
        bufferMemorySize = "10000",
        lingerTime = "0",
        readInRam = "true",
        sendingInterval = "10",
        sendingIntervalTimeUnit = "SECONDS"
      )

      val resultValues1 = ResultValues(100.0, 100.0, 100.0, 100.0, 100.0, 100.0,
        100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)

      val resultValues2 = ResultValues(200.0, 200.0, 200.0, 200.0, 200.0, 200.0,
        200.0, 200.0, 200.0, 200.0, 200.0, 200.0, 200.0)

      val resultRow1 = DatasenderResultRow(configValues, resultValues1)
      val resultRow2 = DatasenderResultRow(configValues, resultValues2)


      val expectedMergedResultValues = ResultValues(150.0, 150.0, 150.0, 150.0, 150.0, 150.0,
        150.0, 150.0, 150.0, 150.0, 150.0, 150.0, 150.0)
      val expectedResultRow = DatasenderResultRow(configValues, expectedMergedResultValues)

      val datasenderSeriesResult = new DatasenderSeriesResult(List(resultRow1, resultRow2))

      assert(expectedResultRow.toTable() == datasenderSeriesResult.toTable())
    }
  }
}
