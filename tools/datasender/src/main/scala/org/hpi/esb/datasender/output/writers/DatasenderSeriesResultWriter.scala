package org.hpi.esb.datasender.output.writers

import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.output.model.SeriesResult
import org.hpi.esb.commons.output.writers.ResultWriter
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.config.ConfigHandler
import org.hpi.esb.datasender.output.model.{DatasenderResultRow, DatasenderSeriesResult}


class DatasenderSeriesResultWriter(inputFilesPrefix: String, resultsPath: String,
                                   outputFileName: String)
  extends ResultWriter(inputFilesPrefix, resultsPath, outputFileName) {

  override def getFinalResult(runResultMaps: List[Map[String, String]]): SeriesResult = {
    val rows: List[DatasenderResultRow] = runResultMaps.map(new DatasenderResultRow(_))
    new DatasenderSeriesResult(rows)
  }
}

object DatasenderSeriesResultWriter extends Logging {
  def main(args: Array[String]): Unit = {
    val inputFilesPrefix = Configs.benchmarkConfig.topicPrefix
    val resultsPath: String = ConfigHandler.resultsPath
    val outputFileName = s"Series_Result_$inputFilesPrefix.csv"

    val seriesResultWriter = new DatasenderSeriesResultWriter(
      inputFilesPrefix = inputFilesPrefix,
      resultsPath = resultsPath,
      outputFileName = outputFileName)

    seriesResultWriter.execute()
  }
}
