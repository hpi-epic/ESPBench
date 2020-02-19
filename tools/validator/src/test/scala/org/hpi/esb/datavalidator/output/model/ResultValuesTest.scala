package org.hpi.esb.datavalidator.output.model

import org.scalatest.FunSpec
import ResultValues._

class ResultValuesTest extends FunSpec {
  val query = "Identity"
  val correct = "true"
  val percentile = "1.3"
  val rtFulfilled = "true"
  val validatorRunTime = "10.0"
  val correctness = "100.0"

  val exampleResultValues = ResultValues(
    query = query,
    correct = correct.toBoolean,
    percentile = percentile.toDouble,
    rtFulfilled = rtFulfilled.toBoolean,
    validatorRunTime = validatorRunTime.toDouble,
    correctness = correctness.toDouble
  )

  describe("toList") {
    it("should return a list representation of the result values") {
      val exampleResultValuesList = exampleResultValues.toList()
      val expectedList = List(query, correct, percentile, rtFulfilled, validatorRunTime, correctness)
      assert(exampleResultValuesList == expectedList)
    }
  }

  describe("fromMap") {
    it("should return a ResultValues object") {
      val valueMap = Map(
        QUERY_COLUMN -> query,
        CORRECT_COLUMN -> correct,
        PERCENTILE_COLUMN -> percentile,
        RT_FULFILLED -> rtFulfilled,
        VALIDATOR_RUNTIME -> validatorRunTime,
        CORRECTNESS -> correctness
      )
      val resultValuesFromMap = new ResultValues(valueMap)

      assert(resultValuesFromMap == exampleResultValues)
    }
  }
}
