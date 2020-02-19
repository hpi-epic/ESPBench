package org.hpi.esb.commons.config

import org.hpi.esb.commons.config.Configs.BenchmarkConfig
import org.scalatest.FunSpec

class BenchmarkConfigTest extends FunSpec {

  val topicPrefix = "ESB"
  val benchmarkRun = 1
  val queryRunConfigs: List[QueryConfig] = List(
    QueryConfig("Identity", 1),
    QueryConfig("Statistics", 1)
  )
  val benchmarkConfig = BenchmarkConfig(topicPrefix, benchmarkRun, queryRunConfigs, kafkaBootstrapServers = "", zookeeperServers = "")

  describe("getTopicName") {
    it("should return the correct topic name") {
      val streamId = 1
      val topicName = benchmarkConfig.getTopicName(topicPrefix, streamId.toString, benchmarkRun.toString)
      val expectedTopicName = s"$topicPrefix-$streamId-$benchmarkRun"
      assert(topicName == expectedTopicName)
    }
  }

  describe("getSourceName") {
    it("should return the correct source topic name") {
      val streamId = 1
      val sourceTopicName = benchmarkConfig.getSourceNameList(queryRunConfigs.head.name, queryRunConfigs.head.numberOfStreams).head
      val expectedSourceTopicName = s"$topicPrefix-$streamId-$benchmarkRun"
      assert(sourceTopicName == expectedSourceTopicName)
    }
  }

  describe("getSinkName") {
    it("should return the correct sink topic name") {
      val streamId = 1
      val query = "Identity"
      val sinkTopicName = benchmarkConfig.getSinkName(queryRunConfigs.head.name)
      val expectedSinkTopicName = s"$topicPrefix-$benchmarkRun-${queryRunConfigs.head.name}"
      assert(sinkTopicName == expectedSinkTopicName)
    }
  }
}
