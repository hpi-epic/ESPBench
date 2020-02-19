package org.hpi.esb.datavalidator.metrics

trait BenchmarkResult {
  def getMeasuredResults: List[String]
  def getResultsHeader: List[String]
}
