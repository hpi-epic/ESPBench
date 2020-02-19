package org.hpi.esb.datavalidator.output.model

import org.hpi.esb.commons.output.Util._
import org.hpi.esb.commons.output.model.SeriesResult
import org.hpi.esb.datavalidator.metrics.ResponseTime

class ValidatorSeriesResult(l: List[ValidatorResultRow]) extends SeriesResult {

  def toTable(): List[List[String]] = {

    val groupedByQuery = l.groupBy(result => result.resultValues.query)

    val mergedQueryResultRows = groupedByQuery.map {
      case (_, queryResultRows) => merge(queryResultRows)
    }.toList

    val header: List[String] = ConfigValues.header ++ ResultValues.header

    header :: mergedQueryResultRows.map(_.toList())
  }

  private def merge(resultRows: List[ValidatorResultRow]): ValidatorResultRow = {

    val configValues = resultRows.head.configValues
    val resultValues = resultRows.map(_.resultValues)

    val averagePercentiles = average(resultValues.map(row => row.percentile))
    val averageRuntime = average(resultValues.map(row => row.validatorRunTime))
    val averageCorrectness = average(resultValues.map(row => row.correctness))

    val mergedResultValues = ResultValues(
      query = resultValues.head.query,
      correct = resultValues.forall(row => row.correct),
      percentile = averagePercentiles,
      rtFulfilled = ResponseTime.fulfillsConstraint(averagePercentiles),
      validatorRunTime = averageRuntime,
      correctness = averageCorrectness
    )

    ValidatorResultRow(configValues, mergedResultValues)
  }
}
