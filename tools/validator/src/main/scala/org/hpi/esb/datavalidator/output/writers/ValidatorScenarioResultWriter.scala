package org.hpi.esb.datavalidator.output.writers

import org.hpi.esb.commons.output.model.ScenarioResult
import org.hpi.esb.commons.output.writers.ResultWriter
import org.hpi.esb.datavalidator.configuration.Config
import org.hpi.esb.datavalidator.output.model.{ValidatorResultRow, ValidatorScenarioResult}

class ValidatorScenarioResultWriter(inputFilesPrefix: String, resultsPath: String,
                                    outputFileName: String)
  extends ResultWriter(inputFilesPrefix, resultsPath, outputFileName) {


  def getFinalResult(seriesResultMaps: List[Map[String, String]]): ScenarioResult = {
    val validatorResultRows = seriesResultMaps.map(resultMap => new ValidatorResultRow(resultMap))
    new ValidatorScenarioResult(validatorResultRows)
  }
}

object ValidatorScenarioResultWriter {
  def main(args: Array[String]): Unit = {
    val inputFilesPrefix: String = "Series_Result"
    val resultsPath: String = Config.resultsPath
    val outputFileName = "Scenario_Result.csv"

    val merger = new ValidatorScenarioResultWriter(
      inputFilesPrefix = inputFilesPrefix,
      resultsPath = resultsPath,
      outputFileName = outputFileName)
    merger.execute()
  }
}
