package org.hpi.esb.datavalidator.configuration

import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.util.Logging
import pureconfig.loadConfig
import pureconfig.generic.auto._

case class ValidatorConfig(consumer: KafkaConsumerConfig, windowSize: Long)

case class KafkaConsumerConfig(autoCommit: String,
                               autoCommitInterval: String, sessionTimeout: String,
                               keyDeserializerClass: String, valueDeserializerClass: String)

object Config extends Logging {

  val relativeValidationPath = "/tools/validator/"
  val validationPath: String = System.getProperty("user.dir") + relativeValidationPath
  val resultsPath = s"$validationPath/results"

  def resultFileName(currentTime: String): String = s"${Configs.benchmarkConfig.topicPrefix}_" +
    s"${Configs.benchmarkConfig.benchmarkRun}_$currentTime.csv"

  val validatorConfig: ValidatorConfig = loadConfig[ValidatorConfig] match {
    case Left(error) => logger.error(error.toString, new Exception(error.toString)); throw new Exception(error.toString)
    case Right(conf) => conf
  }
}

