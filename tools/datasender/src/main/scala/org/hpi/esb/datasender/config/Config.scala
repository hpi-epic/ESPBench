package org.hpi.esb.datasender.config

case class Config(
                   dataReaderConfig: DataReaderConfig,
                   kafkaProducerConfig: KafkaProducerConfig,
                   verbose: Boolean = false) {
  def isValid: Boolean = dataReaderConfig.isValid & kafkaProducerConfig.isValid
}




