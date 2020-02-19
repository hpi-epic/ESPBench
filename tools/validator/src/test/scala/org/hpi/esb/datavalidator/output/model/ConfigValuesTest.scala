package org.hpi.esb.datavalidator.output.model

import org.scalatest.FunSpec
import ConfigValues._
import org.hpi.esb.commons.config.Configs.BenchmarkConfig
import org.hpi.esb.commons.config.QueryConfig

class ConfigValuesTest extends FunSpec {

  val sendingInterval = "100"
  val sendingIntervalTimeUnit = "SECONDS"

  val exampleConfigValues = ConfigValues(sendingInterval, sendingIntervalTimeUnit)

  describe("toList") {
    it("should return a list representation of the config values") {
      val configValuesList = exampleConfigValues.toList()
      val expectedList = List(sendingInterval, sendingIntervalTimeUnit)

      assert(configValuesList == expectedList)
    }
  }

  describe("fromMap") {
    it("should return a ConfigValues object") {
      val valueMap = Map(
        SENDING_INTERVAL -> sendingInterval,
        SENDING_INTERVAL_TIMEUNIT -> sendingIntervalTimeUnit
      )
      val configValuesFromMap = new ConfigValues(valueMap)

      assert(configValuesFromMap == exampleConfigValues)
    }
  }

  describe("get") {
    it("should return the most important config values") {
      val benchmarkConfig = BenchmarkConfig(
        topicPrefix = "ESB",
        benchmarkRun = 0,
        queryConfigs = List[QueryConfig](QueryConfig("Identity", 1), QueryConfig("Statistics", 1)),
        sendingInterval = sendingInterval.toInt,
        sendingIntervalTimeUnit = sendingIntervalTimeUnit,
        kafkaBootstrapServers = "123",
        zookeeperServers = "456"
      )

      val configValues = ConfigValues.get(benchmarkConfig)
      assert(configValues == exampleConfigValues)
    }
  }

  describe("header") {
    val expectedHeader = List(SENDING_INTERVAL, SENDING_INTERVAL_TIMEUNIT)
    assert(ConfigValues.header == expectedHeader)
  }
}
