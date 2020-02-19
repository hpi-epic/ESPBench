package org.hpi.esb.util

import java.util
import java.util.Properties

import kafka.common.TopicAndPartition
import org.apache.kafka.clients.ClientUtils
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.requests.MetadataResponse.TopicMetadata
import org.apache.kafka.common.serialization.StringDeserializer
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.util.Logging
import org.apache.kafka.common.TopicPartition

object OffsetManagement extends Logging {

  def getNumberOfMessages(topic: String, partition: Int): Long = {

    val clientId = "GetOffset"

    val properties = new Properties()
    properties.put("bootstrap.servers", Configs.benchmarkConfig.kafkaBootstrapServers)
    properties.put("group.id", java.util.UUID.randomUUID().toString)
    properties.put("key.deserializer", classOf[StringDeserializer])
    properties.put("value.deserializer", classOf[StringDeserializer])

    val consumer = new KafkaConsumer[String, String](properties)
    val topicAndPartition = new TopicPartition(topic, partition)
    val topicCollection = util.Arrays.asList(topicAndPartition)
    consumer.assign(topicCollection)
    consumer.seekToEnd(topicCollection)
    consumer.position(topicCollection.get(0))
  }

}
