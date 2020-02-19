package org.hpi.esb.commons.config

import java.nio.file.FileSystems
import java.util.concurrent.TimeUnit
import org.hpi.esb.commons.util.Logging
import pureconfig.loadConfig
import pureconfig.generic.auto._
import scala.util.{Failure, Success, Try}

object Configs extends Logging {

  val TopicNameSeparator = "-"
  private val repositoryName = "EnterpriseStreamingBenchmark"
  private val relativeConfigPath = "/tools/commons/commons.conf"
  private val configPath: String = {
    var currDir = new java.io.File(".").getCanonicalPath
    while (currDir != null && !currDir.toLowerCase().endsWith(repositoryName.toLowerCase())) {
      currDir = new java.io.File(currDir).getParentFile.getCanonicalPath
    }
    if (currDir == null) {
      logger.error(s"Could not find repository root directory. Please execute from a directory within the repository. " +
        s"Current directory:\n${new java.io.File(".").getCanonicalPath}")
    }
    currDir + relativeConfigPath
  }
  val benchmarkConfig: BenchmarkConfig = getConfig(configPath)

  def getConfig(configPath: String): BenchmarkConfig = {
    val config = loadConfig[BenchmarkConfig](FileSystems.getDefault.getPath(configPath)) match {
      case Left(f) => logger.error(s"configPath: $configPath\nFailure: ${f.toString}"); sys.exit(1)
      case Right(conf) => conf
    }
    if (!config.isValid) {
      logger.error(s"Invalid configuration:\n${config.toString}")
      sys.exit(1)
    }
    config
  }


  case class InternalQueryConfig(queryName: String = "", inputTopics: List[String] = List[String](), outputTopic: String = "")

  import DefaultValues._

  case class BenchmarkConfig(topicPrefix: String,
                             benchmarkRun: Int,
                             queryConfigs: List[QueryConfig],
                             sendingInterval: Int = defaultSendingInterval,
                             sendingIntervalTimeUnit: String = defaultSendingIntervalTimeUnit,
                             duration: Long = defaultDuration,
                             durationTimeUnit: String = defaultDurationTimeUnit,
                             kafkaBootstrapServers: String,
                             zookeeperServers: String) {

    val fullQueryConfigs: List[InternalQueryConfig] = queryConfigs.map(singleQueryConfig =>
      InternalQueryConfig(singleQueryConfig.name,
        getSourceNameList(singleQueryConfig.name, singleQueryConfig.numberOfStreams),
        getSinkName(singleQueryConfig.name))
    )

    val sourceTopics: List[String] = fullQueryConfigs.flatMap(_.inputTopics).distinct
    val sinkTopics: List[String] = fullQueryConfigs.map(_.outputTopic).distinct
    val topics: List[String] = (sourceTopics ++ sinkTopics).distinct

    def getSourceNameList(query: String, numberOfStreams: Int): List[String] = {
      List.range(1, numberOfStreams + 1).map(idx => {
        //TODO: remove workaround //beautify
        if (query.equalsIgnoreCase(QueryNames.ProcessingTimesQuery)) {
          getTopicName(topicPrefix, benchmarkRun.toString, (idx + 2).toString)
        } else {
          getTopicName(topicPrefix, benchmarkRun.toString, idx.toString)
        }
      }
      )
    }

    def getTopicName(strings: String*): String = {
      strings.mkString(Configs.TopicNameSeparator)
    }

    def getSinkName(query: String): String = {
      getTopicName(topicPrefix, benchmarkRun.toString, query)
    }

    def isValid: Boolean = {
      BenchmarkConfigValidator.isValid(this)
    }

    def getDurationTimeUnit: TimeUnit = TimeUnit.valueOf(durationTimeUnit)

    def getSendingIntervalTimeUnit: TimeUnit = TimeUnit.valueOf(sendingIntervalTimeUnit)

  }

  object BenchmarkConfigValidator {
    def isValid(benchmarkConfig: BenchmarkConfig): Boolean = {
      isValidSendingInterval(benchmarkConfig.sendingInterval) &&
        isTimeUnitValid(benchmarkConfig.sendingIntervalTimeUnit) &&
        isValidDuration(benchmarkConfig.duration) &&
        isTimeUnitValid(benchmarkConfig.durationTimeUnit)
    }

    def isValidDuration(duration: Long): Boolean = duration > 0

    def isValidSendingInterval(sendingInterval: Long): Boolean = sendingInterval > 0

    def isTimeUnitValid(timeUnit: String): Boolean = {
      val timeUnitEnum = Try(TimeUnit.valueOf(timeUnit))
      timeUnitEnum match {
        case Success(_) => true
        case Failure(_) => false
      }
    }

  }

  object QueryNames {
    val IdentityQuery = "Identity"
    val StatisticsQuery = "Statistics"
    val AbsoluteThresholdQuery = "AbsoluteThreshold"
    val MultiThresholdQuery = "MultiThreshold"
    val MachinePowerQuery = "MachinePower"
    val ProcessingTimesQuery = "ProcessingTimes"
    val StochasticOutlierSelection = "StochasticOutlierSelection"
  }

  object DefaultValues {
    val defaultDuration = 5
    val defaultDurationTimeUnit: String = TimeUnit.MINUTES.toString
    val defaultSendingInterval = 10000
    val defaultSendingIntervalTimeUnit: String = TimeUnit.NANOSECONDS.toString
    val defaultSingleColumn = false
  }

}
