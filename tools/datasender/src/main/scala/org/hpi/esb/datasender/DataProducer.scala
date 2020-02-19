package org.hpi.esb.datasender

import java.util.Properties
import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.hpi.esb.commons.config.{Configs, QueryConfig}
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.config.{ConfigHandler, Configurable, DataReaderConfig, KafkaProducerConfig}
import org.hpi.esb.datasender.output.writers.DatasenderRunResultWriter
import org.hpi.esb.util.OffsetManagement

import scala.io.Source

object DataProducer {

  private val kafkaProducer = new KafkaProducer[String, String](createKafkaProducerProperties(ConfigHandler.config.kafkaProducerConfig))

  def createKafkaProducerProperties(kafkaProducerConfig: KafkaProducerConfig): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configs.benchmarkConfig.kafkaBootstrapServers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerConfig.keySerializerClass.get)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerConfig.valueSerializerClass.get)
    props.put(ProducerConfig.ACKS_CONFIG, kafkaProducerConfig.acks.get)
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProducerConfig.batchSize.get.toString)
    props.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProducerConfig.lingerTime.toString)
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProducerConfig.bufferMemorySize.toString)
    props
  }
}

class DataProducer(resultHandler: DatasenderRunResultWriter,
                   dataReaderConfig: DataReaderConfig,
                   sourceTopics: List[String],
                   sendingInterval: Int,
                   sendingIntervalTimeUnit: TimeUnit,
                   duration: Long,
                   durationTimeUnit: TimeUnit) extends Logging with Configurable {

  val numberOfInputTopics: Int = sourceTopics.length
  val executor: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(numberOfInputTopics)
  var producerThreads: List[DataProducerThread] = List[DataProducerThread]()

  sourceTopics.foreach(sourceTopic => {
    val sourceTopicSplit = sourceTopic.split(Configs.TopicNameSeparator)
    //idx length-2 in case topic prefix contains separator; last number in topic name is #benchmark run; -1 as number not idx is used
    var dataInputFileIdx = sourceTopicSplit(sourceTopicSplit.length - 1).toInt - 1
    var dataReader: DataReader = null
    //workaround for sending the correct data for processing times query
    if (dataInputFileIdx == 2) {
      dataInputFileIdx = dataReaderConfig.dataInputPath.length - 1
      dataReader = new DataReader(
        Source.fromFile(dataReaderConfig.dataInputPath(dataInputFileIdx)),
        dataReaderConfig.readInRam,
        false)
    } else {
      dataReader = new DataReader(Source.fromFile(dataReaderConfig.dataInputPath(dataInputFileIdx)), dataReaderConfig.readInRam)
    }
    producerThreads = new DataProducerThread(
      this,
      dataReader,
      sourceTopic,
      duration,
      durationTimeUnit) :: producerThreads
  }
  )

  var ts: List[ScheduledFuture[_]] = _

  def shutDown(): Unit = {
    ts.foreach(t => t.cancel(false))
    producerThreads.foreach(_.dataReader.close())
    getKafkaProducer.close()
    executor.shutdown()
    logger.info("Shut data producer down.")
    val expectedRecordNumber = producerThreads.map(t => t.numberOfRecords).sum
    resultHandler.outputResults(getTopicOffsets, expectedRecordNumber)
  }

  def execute(): Unit = {
    val initialDelay = 0
    ts = producerThreads.map { thread =>
      executor.scheduleAtFixedRate(thread, initialDelay, sendingInterval, sendingIntervalTimeUnit)
    }
    val allTopics = sourceTopics.mkString(" ")
    logger.info(s"Sending records to following topics: $allTopics")
  }

  def getKafkaProducer: KafkaProducer[String, String] = DataProducer.kafkaProducer

  def getTopicOffsets: Map[String, Long] = {
    sourceTopics.map(topic => {
      val currentOffset = OffsetManagement.getNumberOfMessages(topic, partition = 0)
      topic -> currentOffset
    }).toMap[String, Long]
  }
}
