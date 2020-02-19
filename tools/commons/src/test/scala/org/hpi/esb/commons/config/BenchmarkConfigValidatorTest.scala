package org.hpi.esb.commons.config

import java.util.concurrent.TimeUnit

import org.hpi.esb.commons.config.Configs.{BenchmarkConfig, BenchmarkConfigValidator}
import org.scalatest.FunSpec

class BenchmarkConfigValidatorTest extends FunSpec {
  val topicPrefix: String = "ESB"
  val benchmarkRun: Int = 1
  val queryConfigs: List[QueryConfig] = List(
    QueryConfig("Identity", 1),
    QueryConfig("Statistics", 1)
  )

  describe("isTimeUnitValid") {
    it("should return true when a correct string is passed") {
      val validTimeUnits = List("DAYS", "HOURS", "MICROSECONDS", "MILLISECONDS",
        "MINUTES", "NANOSECONDS", "SECONDS")

      validTimeUnits.foreach(timeUnit => {
        val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
          sendingIntervalTimeUnit = timeUnit, durationTimeUnit = timeUnit, kafkaBootstrapServers = "", zookeeperServers = "")
        assert(BenchmarkConfigValidator.isValid(config))
      })
    }

    it("should return false if a wrong string is passed") {
      val incorrectTimeUnit = "abcdef"
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        sendingIntervalTimeUnit = incorrectTimeUnit, durationTimeUnit = incorrectTimeUnit, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(!BenchmarkConfigValidator.isValid(config))

    }
  }
  describe("isValidSendingInterval") {
    it("should return false if sending interval is < 0") {
      val sendingInterval = -1
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        sendingInterval = sendingInterval, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(!BenchmarkConfigValidator.isValid(config))
    }

    it("should return false if sending interval  is 0") {
      val sendingInterval = 0
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        sendingInterval = sendingInterval, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(!BenchmarkConfigValidator.isValid(config))
    }

    it("should return true if sending interval is positive") {
      val sendingInterval = 1
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        sendingInterval = sendingInterval, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(BenchmarkConfigValidator.isValid(config))
    }
  }

  describe("getSendingIntervalTimeUnit") {
    it("should return the correct sending interval time unit") {
      val sendingIntervalTimeUnit = TimeUnit.MINUTES.toString
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        sendingIntervalTimeUnit = sendingIntervalTimeUnit, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(config.getSendingIntervalTimeUnit == TimeUnit.MINUTES)
    }
  }

  describe("getDurationTimeUnit") {
    it("should return the correct sending interval time unit") {
      val durationTimeUnit = TimeUnit.MINUTES.toString
      val config = BenchmarkConfig(topicPrefix, benchmarkRun, queryConfigs,
        durationTimeUnit = durationTimeUnit, kafkaBootstrapServers = "", zookeeperServers = "")
      assert(config.getDurationTimeUnit == TimeUnit.MINUTES)
    }
  }

}
