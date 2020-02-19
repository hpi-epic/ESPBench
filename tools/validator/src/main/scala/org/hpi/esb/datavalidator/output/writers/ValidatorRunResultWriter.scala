package org.hpi.esb.datavalidator.output.writers

import java.text.SimpleDateFormat
import java.util.Date

import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.output.{CSVOutput, Tabulator}
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datavalidator.configuration.Config.{resultFileName, resultsPath}
import org.hpi.esb.datavalidator.output.model.{ConfigValues, ResultValues}
import org.hpi.esb.datavalidator.validation.QueryValidationState

class ValidatorRunResultWriter extends Logging {

  val currentTimeString: String = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())

  def outputResults(queryValidationStates: List[QueryValidationState], startTime: Long): Unit = {

    val table = createOutputTable(queryValidationStates, startTime)
    CSVOutput.write(table, resultsPath, resultFileName(currentTimeString))
    logger.info(Tabulator.format(table))
  }

  def createOutputTable(queryValidationStates: List[QueryValidationState], startTime: Long): List[List[String]] = {
    val configValuesHeader = ConfigValues.header
    val configValues = ConfigValues.get(Configs.benchmarkConfig).toList()

    val resultValuesHeader = QueryValidationState.getHeader ++ List(ResultValues.VALIDATOR_RUNTIME)
    val resultValues = queryValidationStates.map(
      queryValidationState =>
        getResultValues(queryValidationState, queryValidationState.validationFinishedTs - startTime)
    )

    val header = configValuesHeader ++ resultValuesHeader
    val rows = resultValues.map(resultValueRow => configValues ++ resultValueRow)

    val table = header :: rows
    table
  }

  def getResultValues(queryValidationState: QueryValidationState, runTime: Long): List[String] = {
    queryValidationState.getMeasuredResults ++ List(runTime.toString)
  }
}
