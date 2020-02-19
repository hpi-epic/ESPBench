package org.hpi.esb.datasender.output.model

import org.hpi.esb.commons.config.Configs.BenchmarkConfig
import org.hpi.esb.datasender.config.Config

object ConfigValues {
  val BATCH_SIZE = "batchSize"
  val BUFFER_MEMORY_SIZE = "bufferMemorySize"
  val LINGER_TIME = "lingerTime"
  val READ_IN_RAM = "readInRam"
  val SENDING_INTERVAL = "sendingInterval"
  val SENDING_INTERVAL_TIMEUNIT = "sendingIntervalTimeUnit"

  val header = List(BATCH_SIZE, BUFFER_MEMORY_SIZE, LINGER_TIME, READ_IN_RAM, SENDING_INTERVAL, SENDING_INTERVAL_TIMEUNIT)

  def get(config: Config, benchmarkConfig: BenchmarkConfig): ConfigValues = {
    ConfigValues(
      batchSize = config.kafkaProducerConfig.batchSize.get.toString,
      bufferMemorySize = config.kafkaProducerConfig.bufferMemorySize.toString,
      lingerTime = config.kafkaProducerConfig.lingerTime.toString,
      readInRam = config.dataReaderConfig.readInRam.toString,
      sendingInterval = benchmarkConfig.sendingInterval.toString,
      sendingIntervalTimeUnit = benchmarkConfig.sendingIntervalTimeUnit)
  }
}

import ConfigValues._

case class ConfigValues(batchSize: String, bufferMemorySize: String, lingerTime: String,
                        readInRam: String, sendingInterval: String, sendingIntervalTimeUnit: String) {

  def this(m: Map[String, String]) = this(m(BATCH_SIZE), m(BUFFER_MEMORY_SIZE), m(LINGER_TIME),
    m(READ_IN_RAM), m(SENDING_INTERVAL), m(SENDING_INTERVAL_TIMEUNIT))

  def toList: List[String] = {
    List(batchSize.toString, bufferMemorySize.toString, lingerTime.toString, readInRam.toString,
      sendingInterval.toString, sendingIntervalTimeUnit)
  }
}
