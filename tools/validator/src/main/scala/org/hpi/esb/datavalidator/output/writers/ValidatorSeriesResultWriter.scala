package org.hpi.esb.datavalidator.output.writers

import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.output.model.SeriesResult
import org.hpi.esb.commons.output.writers.ResultWriter
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datavalidator.configuration.Config
import org.hpi.esb.datavalidator.output.model.{ValidatorResultRow, ValidatorSeriesResult}


class ValidatorSeriesResultWriter(inputFilesPrefix: String, resultsPath: String,
                                  outputFileName: String)
  extends ResultWriter(inputFilesPrefix, resultsPath, outputFileName) with Logging {


  override def getFinalResult(runResultMaps: List[Map[String, String]]): SeriesResult = {
    val rows: List[ValidatorResultRow] = runResultMaps.map(new ValidatorResultRow(_))
    new ValidatorSeriesResult(rows)
  }
}

object ValidatorSeriesResultWriter extends Logging {
  def main(args: Array[String]): Unit = {

    val inputFilesPrefix = Configs.benchmarkConfig.topicPrefix
    val resultsPath: String = Config.resultsPath
    val outputFileName = s"Series_Result_$inputFilesPrefix.csv"

    val merger = new ValidatorSeriesResultWriter(
      inputFilesPrefix = inputFilesPrefix,
      resultsPath = resultsPath,
      outputFileName = outputFileName)

    merger.execute()
  }
}
