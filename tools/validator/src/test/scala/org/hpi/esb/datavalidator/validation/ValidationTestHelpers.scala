package org.hpi.esb.datavalidator.validation

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ClosedShape, Graph, SourceShape}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.record.TimestampType
import org.apache.kafka.common.serialization.StringDeserializer
import org.hpi.esb.datavalidator.data.{SimpleRecord, Statistics}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatchers, Mockito}

import scala.concurrent.Future
import scala.concurrent.duration._

trait ValidationTestHelpers {

  type OptionPair[T] = (Option[T], Option[T])
  type StringRec = ConsumerRecord[String, String]

  def addTestSink[T](partial: Graph[SourceShape[OptionPair[T]], NotUsed], system: ActorSystem) = {

    val testSink = TestSink.probe[OptionPair[T]](system)

    GraphDSL.create(testSink) { implicit builder =>
      sink =>
        import GraphDSL.Implicits._
        val source = builder.add(partial)

        source ~> sink

        ClosedShape
    }
  }

  def combineSourceWithSink[T](source: Source[OptionPair[T], NotUsed],
                               sink: Sink[OptionPair[T], Future[QueryValidationState]]):
  Graph[ClosedShape.type, Future[QueryValidationState]] = {

    GraphDSL.create(sink) { implicit builder =>
      s =>
        import GraphDSL.Implicits._

        source ~> s
        ClosedShape
    }
  }

  def createTopicHandler(topic: String, values: List[(Long, String)]): TopicHandler = {
    val records = createConsumerRecordList(topic, values)

    val source = testSource(topic, records)
    new TopicHandler(topic, records.length, source)
  }

  def createConsumerRecordList(topic: String, values: List[(Long, String)]): List[StringRec] = {
    def createList(values: List[(Long, String)]): List[StringRec] = {
      values match {
        case Nil => Nil
        case (timestamp, value) :: tail => createConsumerRecord(topic, timestamp, value) :: createList(tail)
      }
    }

    createList(values)
  }

  def createConsumerRecord(topic: String, timestamp: Long, value: String): StringRec = {
    val partition = 0
    val offset = 0
    val checksum = 0
    val serializedKeySize = 0
    val serializedValueSize = 0
    val key = "0"

    new ConsumerRecord[String, String](topic, partition, offset, timestamp, TimestampType.CREATE_TIME, checksum, serializedKeySize, serializedValueSize, key, value)
  }

  def createSimpleRecordsList(values: List[(Long, String)]) = {
    values.map { case (timestamp, value) => Some(SimpleRecord.deserialize(value, timestamp)) }
  }

  def createStatisticsList(values: List[(Long, String)]) = {
    values.map { case (timestamp, value) => Some(Statistics.deserialize(value, timestamp)) }
  }

  def testSource(topicName: String, records: List[StringRec]): Source[StringRec, Control] = {

    val mockConsumer = Mockito.mock(classOf[KafkaConsumer[String, String]])

    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    val m = Map[TopicPartition, java.util.List[StringRec]](new TopicPartition(topicName, 0) -> records.asJava)
    val emptyRecordsMap = Map[TopicPartition, java.util.List[StringRec]]()
    val cr = new ConsumerRecords[String, String](m)
    val empty = new ConsumerRecords[String, String](emptyRecordsMap)

    when(mockConsumer.poll(ArgumentMatchers.any[Long]))
      .thenReturn(cr)
      .thenReturn(empty)

    val settings = new ConsumerSettings(
      Map(ConsumerConfig.GROUP_ID_CONFIG -> "GROUP_ID"),
      Some(new StringDeserializer),
      Some(new StringDeserializer),
      1.milli,
      1.milli,
      1.second,
      1.second,
      1.second,
      5.seconds,
      3,
      5.seconds,
      "akka.kafka.default-dispatcher",
      5.seconds,
      false,
      5.seconds
    ) {
      override def createKafkaConsumer(): KafkaConsumer[String, String] = {
        mockConsumer
      }
    }
    val partition = 0
    val subscription = Subscriptions.assignmentWithOffset(
      new TopicPartition(topicName, partition) -> 0L
    )
    Consumer.plainSource(settings, subscription)
  }
}
