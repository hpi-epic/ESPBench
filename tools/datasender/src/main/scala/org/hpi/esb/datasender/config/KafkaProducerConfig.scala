package org.hpi.esb.datasender.config

import KafkaDefaultValues._
import org.hpi.esb.commons.config.Configs
case class KafkaProducerConfig(
                               keySerializerClass: Option[String],
                               valueSerializerClass: Option[String],
                               acks: Option[String],
                               batchSize: Option[Int],
                               bufferMemorySize: Long = defaultBufferMemorySize,
                               lingerTime: Int = defaultLingerTime) extends Configurable {

  def isValid: Boolean = areServerAndSerializerAttributesValid && isAcksValid && isBatchSizeValid

  def areServerAndSerializerAttributesValid: Boolean =
      checkAttributeOptionHasValue("key serializer class", keySerializerClass) &&
      checkAttributeOptionHasValue("value serializer class", valueSerializerClass)

  def isAcksValid: Boolean = acks match {
    case Some("0" | "1" | "-1" | "all") => true
    case _ =>
      logger.error("Config invalid: acks must be either 0, 1, -1 or \"all\"")
      false
  }

  def isBatchSizeValid: Boolean = batchSize.isDefined && checkGreaterOrEqual("batch size", batchSize.get, 0)

  override def toString: String = {
    val prefix = "datasender.kafkaProducer"

    s"""
$prefix.bootstrapServers = ${Configs.benchmarkConfig.kafkaBootstrapServers}
$prefix.keySerializerClass = ${opToStr(keySerializerClass)}
$prefix.valueSerializerClass = ${opToStr(valueSerializerClass)}
$prefix.acks = ${opToStr(acks)}
$prefix.batchSize = ${opToStr(batchSize)}"""
  }
}

object KafkaDefaultValues {
  val defaultBufferMemorySize = 33554432
  val defaultLingerTime = 0
}
