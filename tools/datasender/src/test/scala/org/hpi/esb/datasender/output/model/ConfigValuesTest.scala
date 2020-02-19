package org.hpi.esb.datasender.output.model

import org.hpi.esb.commons.config.Configs.BenchmarkConfig
import org.hpi.esb.commons.config.QueryConfig
import org.hpi.esb.datasender.config.{Config, DataReaderConfig, KafkaProducerConfig}
import org.hpi.esb.datasender.output.model.ConfigValues._
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar

class ConfigValuesTest extends FunSpec with MockitoSugar {

  val batchSize = "1000"
  val bufferMemorySize = "1000"
  val lingerTime = "0"
  val readInRam = "true"
  val sendingInterval = "10"
  val sendingIntervalTimeUnit = "SECONDS"
  val scaleFactor = "1"
  val kafkaBootstrapServers = "192.168.30.208:9092"
  val zookeeperServers = "192.168.30.208:2181,192.168.30.207:2181,192.168.30.141:2181"

  val exampleConfigValues = ConfigValues(batchSize, bufferMemorySize, lingerTime, readInRam,
    sendingInterval, sendingIntervalTimeUnit)

  describe("toList") {
    it("should return a list representation of the config values") {
      val configValuesList = exampleConfigValues.toList
      val expectedList = List(batchSize, bufferMemorySize, lingerTime, readInRam,
        sendingInterval, sendingIntervalTimeUnit)

      assert(configValuesList == expectedList)
    }
  }

  describe("fromMap") {
    it("should return a ConfigValues object") {
      val valueMap = Map(
        BATCH_SIZE -> batchSize,
        BUFFER_MEMORY_SIZE -> bufferMemorySize,
        LINGER_TIME -> lingerTime,
        READ_IN_RAM -> readInRam,
        SENDING_INTERVAL -> sendingInterval,
        SENDING_INTERVAL_TIMEUNIT -> sendingIntervalTimeUnit
      )
      val configValuesFromMap = new ConfigValues(valueMap)

      assert(configValuesFromMap == exampleConfigValues)
    }
  }

  describe("get") {
    it("should return the most important config values") {
      val queryConfigs = List(QueryConfig("mockName", 1))

      val kafkaProducerConfig = KafkaProducerConfig(
        keySerializerClass = Some("keySerializerClass"),
        valueSerializerClass = Some("valueSerializerClass"),
        acks = Some("0"),
        batchSize = Some(batchSize.toInt),
        bufferMemorySize = bufferMemorySize.toLong,
        lingerTime = lingerTime.toInt
      )

      val dataReaderConfig = DataReaderConfig(
        dataInputPath = List[String]("/path"),
        readInRam = readInRam.toBoolean
      )
      val config: Config = Config(dataReaderConfig, kafkaProducerConfig)

      val benchmarkConfig = BenchmarkConfig(
        topicPrefix = "ESB",
        benchmarkRun = 0,
        queryConfigs = queryConfigs,
        sendingInterval = sendingInterval.toInt,
        sendingIntervalTimeUnit = sendingIntervalTimeUnit,
        kafkaBootstrapServers = kafkaBootstrapServers,
        zookeeperServers = zookeeperServers
      )

      val configValues = ConfigValues.get(config, benchmarkConfig)

      assert(configValues == exampleConfigValues)
    }
  }

  describe("header") {
    val expectedHeader = List(BATCH_SIZE, BUFFER_MEMORY_SIZE, LINGER_TIME, READ_IN_RAM,
      SENDING_INTERVAL, SENDING_INTERVAL_TIMEUNIT)
    assert(ConfigValues.header == expectedHeader)
  }
}
