package org.hpi.esb.datavalidator.metrics

import org.scalatest.FunSpec

class ResultCountTest extends FunSpec {

  val inputSize: Long = 1.toLong

  describe("column size matches header size") {
    val resultCount = new ResultCount(inputSize)
    assert(resultCount.getMeasuredResults.lengthCompare(resultCount.getResultsHeader.size) == 0)
  }

  describe("increment") {
    val resultCount = new ResultCount(inputSize)

    it ("should have zero initialized count values") {
      assert(compareOverallCount(resultCount, 0))
    }

    it ("should increment all counts") {
      resultCount.increment(CountType.Error)
      resultCount.increment(CountType.Actual)
      resultCount.increment(CountType.Expected)
      assert(compareOverallCount(resultCount, 1))
    }
  }

  describe("computeCorrectness") {
    it ("should return full correctness if nothing has failed") {
      val resultCount = new ResultCount(inputSize)
      resultCount.increment(CountType.Expected)
      resultCount.increment(CountType.Actual)
      assert(100 == resultCount.computeCorrectness())
    }

    it ("should count every actual offset as an error") {
      val resultCount = new ResultCount(inputSize)
      1 to 2 foreach {_ => resultCount.increment(CountType.Expected)}
      1 to 3 foreach {_ => resultCount.increment(CountType.Actual)}
      assert(50 == resultCount.computeCorrectness())
    }

    it ("should return percentage between, the difference of actual and errors, and expected") {
      val resultCount = new ResultCount(inputSize)
      1 to 4 foreach {_ => resultCount.increment(CountType.Expected)}
      1 to 3 foreach {_ => resultCount.increment(CountType.Actual)}
      resultCount.increment(CountType.Error)
      assert(50 == resultCount.computeCorrectness())
    }

    it ("should not return a negative number") {
      val resultCount = new ResultCount(inputSize)
      resultCount.increment(CountType.Expected)
      1 to 10 foreach {_ => resultCount.increment(CountType.Actual)}
      assert(resultCount.computeCorrectness() >= 0)
    }

    it ("should return the best score if expected and actual are zero") {
      val resultCount = new ResultCount(inputSize)
      assert(resultCount.computeCorrectness() == 100)
    }

    it ("should subtract every error from actual even if actual and expected are equal") {
      val resultCount = new ResultCount(inputSize)
      1 to 4 foreach {_ =>
        resultCount.increment(CountType.Expected)
        resultCount.increment(CountType.Actual)
      }
      resultCount.increment(CountType.Error)
      assert(resultCount.computeCorrectness() == 75)

    }
  }

  private def compareOverallCount(resultCount: ResultCount, threshold: Int): Boolean = {
    List(resultCount.expected, resultCount.actual, resultCount.errors).forall(_ == threshold)
  }
}
