package org.hpi.esb.datasender.metrics

import org.apache.kafka.clients.producer.KafkaProducer
import org.hpi.esb.commons.util.Logging

class MetricHandler(kafkaProducer: KafkaProducer[String, String], topicStartOffsets: Map[String, Long],
                    expectedRecordNumber: Long) extends Logging {


  def fetchMetrics(): Map[String, String] = {
    // get all metrics produced by the kafka producer module
    val kafkaProducerMetrics = new KafkaProducerMetrics(kafkaProducer)
    val kafkaProducerMetricsValues = kafkaProducerMetrics.getMetrics()

    // get all metrics produced by the data sender
    val sendMetrics = new SendMetrics(topicStartOffsets, expectedRecordNumber)
    val sendMetricsValues = sendMetrics.getMetrics()

    kafkaProducerMetricsValues ++ sendMetricsValues
  }
}
