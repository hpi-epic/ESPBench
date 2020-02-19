package org.hpi.esb.datasender

import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.config._
import org.hpi.esb.datasender.output.writers.DatasenderRunResultWriter

import scala.io.Source

class DataDriver extends Logging {

  private val config = ConfigHandler.config
  private val dataProducer = createDataProducer
  private val resultHandler = new DatasenderRunResultWriter(config, Configs.benchmarkConfig, dataProducer.getKafkaProducer)

  def run(): Unit = {
    dataProducer.execute()
  }

  def createDataProducer: DataProducer = {
    val sendingInterval = Configs.benchmarkConfig.sendingInterval
    val sendingIntervalTimeUnit = Configs.benchmarkConfig.getSendingIntervalTimeUnit
    val duration = Configs.benchmarkConfig.duration
    val durationTimeUnit = Configs.benchmarkConfig.getDurationTimeUnit

    new DataProducer(resultHandler, config.dataReaderConfig, Configs.benchmarkConfig.sourceTopics,
      sendingInterval, sendingIntervalTimeUnit, duration, durationTimeUnit)
  }
}
