package org.hpi.esb.datasender.config

import org.apache.log4j.Logger
import org.mockito.Mockito.{times, verify}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class KafkaProducerConfigTest extends FlatSpec with Matchers with MockitoSugar {

  val server = Option("192.0.0.1:1234")
  val keySerializer = Option("keySerializer")
  val valueSerializer = Option("valueSerializer")
  val acks = Option("0")
  val batchSize = Option(100)

  "KafkaProducerConfig.isValid" should "return false if attribute keySerializer is empty" in {
    val invalidKeySerializer = None
    val config = KafkaProducerConfig(invalidKeySerializer, valueSerializer, acks, batchSize)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if attribute valueSerializer is empty" in {
    val invalidValueSerializer = None
    val config = KafkaProducerConfig(keySerializer, invalidValueSerializer, acks, batchSize)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if attribute acks is empty" in {
    val invalidAcks = None
    val config = KafkaProducerConfig(keySerializer, valueSerializer, invalidAcks, batchSize)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if attribute acks contains invalid value" in {
    val invalidAcks = Option("-100000")
    val config = KafkaProducerConfig(keySerializer, valueSerializer, invalidAcks, batchSize)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return true if acks == 1" in {
    val validAcks = Option("1")
    val config = KafkaProducerConfig(keySerializer, valueSerializer, validAcks, batchSize)
    config.logger = mock[Logger]

    assert(config.isValid)
    verify(config.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return true if acks == 0" in {
    val config = KafkaProducerConfig(keySerializer, valueSerializer, acks, batchSize)
    config.logger = mock[Logger]

    assert(config.isValid)
    verify(config.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return true if acks == -1" in {
    val validAcks = Option("-1")
    val config = KafkaProducerConfig(keySerializer, valueSerializer, validAcks, batchSize)
    config.logger = mock[Logger]

    assert(config.isValid)
    verify(config.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return true if acks == 'all'" in {
    val validAcks = Option("all")
    val config = KafkaProducerConfig(keySerializer, valueSerializer, validAcks, batchSize)
    config.logger = mock[Logger]

    assert(config.isValid)
    verify(config.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return true if batch size >= 0" in {
    val config = KafkaProducerConfig(keySerializer, valueSerializer, acks, batchSize)
    config.logger = mock[Logger]

    assert(config.isValid)
    verify(config.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if batch size < 0" in {
    val invalidBatchSize = Option(-1)
    val config = KafkaProducerConfig(keySerializer, valueSerializer, acks, invalidBatchSize)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

}
