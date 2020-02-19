package org.hpi.esb.datasender.config

import java.nio.file.{FileSystems, Path}
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.util.Logging
import pureconfig.loadConfig
import pureconfig.generic.auto._

object ConfigHandler extends Logging {
  val projectPath = System.getProperty("user.dir")
  val dataSenderPath = s"$projectPath/tools/datasender"
  val configName = "datasender.conf"
  val userConfigPath = s"$dataSenderPath/$configName"
  val resultsPath = s"$dataSenderPath/results"
  val config: Config = getConfig()

  def resultFileName(currentTime: String): String = s"${Configs.benchmarkConfig.topicPrefix}_" +
    s"${Configs.benchmarkConfig.benchmarkRun}_$currentTime.csv"

  def resultFileNamePrefix(): String = s"${Configs.benchmarkConfig.topicPrefix}"

  private def getConfig(): Config = {
    if (!FileSystems.getDefault.getPath(userConfigPath).toFile.exists && FileSystems.getDefault.getPath(userConfigPath).toFile.isFile) {
      logger.error(s"The config file '$userConfigPath' does not exist.")
      sys.exit(1)
    }

    val config = loadConfig[Config](FileSystems.getDefault.getPath(userConfigPath)) match {
      case Left(f) => {
        logger.error(s"Invalid configuration for file $userConfigPath")
        logger.error(f.toString)
        sys.exit(1)
      }
      case Right(conf) => conf
    }

    if (!config.isValid) {
      logger.error(s"Invalid configuration:\n${config.toString}")
      sys.exit(1)
    }
    config
  }
}
