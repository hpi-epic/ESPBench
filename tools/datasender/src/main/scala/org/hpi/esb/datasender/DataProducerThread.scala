package org.hpi.esb.datasender

import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.hpi.esb.commons.util.Logging

class DataProducerThread(dataProducer: DataProducer,
                         val dataReader: DataReader,
                         topic: String,
                         duration: Long,
                         durationTimeUnit: TimeUnit)
  extends Runnable with Logging {

  var numberOfRecords: Int = 0
  val startTime: Long = currentTime
  val endTime: Long = startTime + durationTimeUnit.toMillis(duration)

  private var topicMsgIdList: Map[String, Int] = Map[String, Int]()

  def getNextMessageId(topic: String): Int = {
    var id = 0
    if (topicMsgIdList.contains(topic)) {
      id = topicMsgIdList(topic)
      id += 1
    }
    topicMsgIdList += (topic -> id)
    id
  }

  def currentTime: Long = System.currentTimeMillis()

  def run() {
    val msg = dataReader.readRecord
    if (currentTime < endTime && msg.nonEmpty) {
      send(msg.get)
    } else {
      logger.info(s"Shut down after $durationTimeUnit: $duration.")
      dataProducer.shutDown()
    }
  }

  def send(message: String): Unit = {
    sendToKafka(topic, message)
  }

  def sendToKafka(topic: String, message: String): Unit = {
    val msgWithIdAndTs = s"${getNextMessageId(topic)};$currentTime;$message"
    val record = new ProducerRecord[String, String](topic, msgWithIdAndTs)
    dataProducer.getKafkaProducer.send(record)
    logger.debug(s"Sent value $msgWithIdAndTs to topic $topic.")
  }
}
