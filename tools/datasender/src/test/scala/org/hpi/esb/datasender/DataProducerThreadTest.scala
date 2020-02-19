package org.hpi.esb.datasender

import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.{never, times, verify}
import org.mockito.{ArgumentMatchers, Mockito}
import scala.io.Source

class DataProducerThreadTest extends FunSpec with MockitoSugar {

  val topicA = "ESB_A_1"
  val ts: Long = 999
  val initialTopicId = 0
  var mockedDataReader: DataReader = mock[DataReader]
  val duration = 10
  val durationTimeUnit = TimeUnit.MINUTES

  describe("send") {

    it("should increment message id correctly") {
      val mockedKafkaProducer: KafkaProducer[String, String] = mock[KafkaProducer[String, String]]
      val mockedDataProducer: DataProducer = mock[DataProducer]
      val records = List("dat0Topic0", "dat1")
      val spyDP = Mockito.spy(mockedDataProducer)
      Mockito.doReturn(mockedKafkaProducer, Nil: _*).when(spyDP).getKafkaProducer
      val dataProducerThreadSC = new DataProducerThread(spyDP,
        mockedDataReader, topicA, duration, durationTimeUnit)
      val spySC = Mockito.spy(dataProducerThreadSC)
      Mockito.doReturn(ts, Nil: _*).when(spySC).currentTime

      spySC.send(records.head)
      verify(mockedKafkaProducer, times(1)).send(new ProducerRecord[String, String](topicA, s"$initialTopicId;$ts;" + records.head))
      spySC.send(records(1))
      verify(mockedKafkaProducer, times(1)).send(new ProducerRecord[String, String](topicA, s"${initialTopicId + 1};$ts;" + records(1)))
    }
  }

  describe("run") {

    val rec1 = "ts id dat00 dat01 dat02"
    val duration = 1
    val durationTimeUnit = TimeUnit.MINUTES

    it("should send records if the end time has not been reached") {
      val source: Source = Source.fromString(s"$rec1")
      val dataReader = new DataReader(source, readInRam = false)
      val mockedDataProducer: DataProducer = mock[DataProducer]
      val mockedKafkaProducer = mock[KafkaProducer[String, String]]
      val spyDP = Mockito.spy(mockedDataProducer)
      Mockito.doReturn(mockedKafkaProducer, Nil: _*).when(spyDP).getKafkaProducer
      val dataProducerThread = new DataProducerThread(spyDP,
        dataReader, topicA, duration, durationTimeUnit)
      val spy = Mockito.spy(dataProducerThread)
      Mockito.doReturn(ts, Nil: _*).when(spy).currentTime
      spy.run()
      verify(mockedKafkaProducer, times(1)).send(new ProducerRecord[String, String](topicA, s"$initialTopicId;$ts;$rec1"))
    }

    it("should not send records if the time end has been reached") {
      val mockedDataProducer: DataProducer = mock[DataProducer]
      val mockedKafkaProducer = mock[KafkaProducer[String, String]]
      val spyDP = Mockito.spy(mockedDataProducer)
      Mockito.doReturn(mockedKafkaProducer, Nil: _*).when(spyDP).getKafkaProducer
      val source: Source = Source.fromString("")
      val dataReader = new DataReader(source, readInRam = false)
      val dataProducerThread = new DataProducerThread(spyDP,
        dataReader, topicA, duration, durationTimeUnit)
      val spy = Mockito.spy(dataProducerThread)
      Mockito.doReturn(System.currentTimeMillis() * 1000, Nil: _*).when(spy).currentTime
      spy.run()
      verify(mockedKafkaProducer, never()).send(ArgumentMatchers.any[ProducerRecord[String, String]])
      verify(spyDP, times(1)).shutDown()
    }
  }
}
