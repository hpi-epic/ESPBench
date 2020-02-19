package org.hpi.esb.datasender.metrics

import org.apache.kafka.clients.producer.KafkaProducer
import org.hpi.esb.commons.output.Util._
import scala.collection.JavaConverters._

class KafkaProducerMetrics(kafkaProducer: KafkaProducer[String, String]) extends Metric {
  val desiredMetrics = List("batch-size-avg", "record-send-rate", "records-per-request-avg",
    "record-error-rate", "record-queue-time-avg", "buffer-exhausted-rate",
    "bufferpool-wait-ratio", "request-latency-max", "waiting-threads",
    "buffer-available-bytes")

  override def getMetrics(): Map[String, String] = filterMetric(desiredMetrics)

  /**
    * filter the desired metric from all available kafka producer metrics
    * per topic and overall
    *
    * @param desiredMetrics
    * @return
    */
  def filterMetric(desiredMetrics: List[String]): Map[String, String] = {
    val accMetrics = Map[String, String]()
    kafkaProducer.metrics().asScala.foldLeft(accMetrics) {
      case (acc, (metricName, metric)) => {
        if (desiredMetrics.contains(metricName.name()) &&
          metricName.group() == "producer-metrics") {
          val key = metricName.name()
          val value = round(metric.value(), precision = 2)
          acc ++ Map[String, String](key -> value.toString)
        } else {
          acc
        }
      }
      case (acc, _) => acc
    }
  }

}
