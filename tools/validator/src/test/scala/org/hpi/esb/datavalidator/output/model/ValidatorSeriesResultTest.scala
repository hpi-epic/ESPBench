package org.hpi.esb.datavalidator.output.model

import org.scalatest.FunSpec

class ValidatorSeriesResultTest extends FunSpec {

  describe("ValidatorSeriesResultTest") {

    it("should merge") {
      val configValues = ConfigValues(
        sendingInterval = "100",
        sendingIntervalUnit = "SECONDS"
      )

      val resultValues1 = ResultValues(
        query = "Identity",
        correct = true,
        percentile = 1.3,
        rtFulfilled = true,
        validatorRunTime = 10.0,
        correctness = 100.0
      )
      val resultRow1 = ValidatorResultRow(configValues, resultValues1)

      val resultValues2 = ResultValues(
        query = "Identity",
        correct = false,
        percentile = 1.4,
        rtFulfilled = true,
        validatorRunTime = 20.0,
        correctness = 0.0
      )
      val resultRow2 = ValidatorResultRow(configValues, resultValues2)

      val expectedMergedResultValue = ResultValues(
        query = "Identity",
        correct = false,
        percentile = 1.35,
        rtFulfilled = true,
        validatorRunTime = 15.0,
        correctness = 50.0
      )

      val expectedResultRow = ValidatorResultRow(configValues, expectedMergedResultValue)

      val validatorSeriesResult = new ValidatorSeriesResult(List(resultRow1, resultRow2))

      assert(expectedResultRow.toTable() == validatorSeriesResult.toTable())

    }

  }
}
