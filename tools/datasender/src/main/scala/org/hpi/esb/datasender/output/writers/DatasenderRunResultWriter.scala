package org.hpi.esb.datasender.output.writers

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.kafka.clients.producer.KafkaProducer
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.config.Configs.BenchmarkConfig
import org.hpi.esb.commons.output.{CSVOutput, Tabulator}
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.config._
import org.hpi.esb.datasender.metrics.MetricHandler
import org.hpi.esb.datasender.output.model.{ConfigValues, DatasenderResultRow, ResultValues}

class DatasenderRunResultWriter(config: Config, benchmarkConfig: BenchmarkConfig,
                                kafkaProducer: KafkaProducer[String, String]) extends Logging {

  val currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())

  def outputResults(topicOffsets: Map[String, Long], expectedRecordNumber: Int): Unit = {
    val metricHandler = new MetricHandler(kafkaProducer, topicOffsets, expectedRecordNumber)
    val metrics = metricHandler.fetchMetrics()

    val configValues = ConfigValues.get(ConfigHandler.config, Configs.benchmarkConfig)
    val resultValues = new ResultValues(metrics)

    val dataSenderResultRow = DatasenderResultRow(configValues, resultValues)

    val table = dataSenderResultRow.toTable()
    CSVOutput.write(table, ConfigHandler.resultsPath, ConfigHandler.resultFileName(currentTime))
    logger.info(Tabulator.format(table))
  }
}
