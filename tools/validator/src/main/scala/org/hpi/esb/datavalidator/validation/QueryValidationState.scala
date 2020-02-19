package org.hpi.esb.datavalidator.validation

import org.hpi.esb.datavalidator.metrics.CountType.CountType
import org.hpi.esb.datavalidator.metrics.{Correctness, ResponseTime, ResultCount}

object QueryValidationState {
  def getHeader: List[String] = List("Query", "Topic") ++ Correctness.header ++ ResponseTime.header ++ ResultCount.header
}

class QueryValidationState(val query: String, topicName: String,
                           val resultCount: ResultCount,
                           val correctness: Correctness = new Correctness(),
                           val responseTime: ResponseTime = new ResponseTime(),
                           var validationFinishedTs: Long = 0
                          ) {

  def updateCorrectness(isCorrect: Boolean): Unit = {
    correctness.update(isCorrect)
  }

  def updateResponseTime(value: Long): Unit = {
    responseTime.updateValue(value)
  }

  def updateResultCount(countType: CountType): Unit = {
    resultCount.increment(countType)
  }

  def fulfillsConstraints(): Boolean = correctness.fulfillsConstraint &&
    responseTime.fulfillsConstraint


  def getMeasuredResults: List[String] = {
    List(query, topicName) ++ correctness.getMeasuredResults ++ responseTime.getMeasuredResults ++ resultCount.getMeasuredResults
  }
}
