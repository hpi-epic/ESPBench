package org.hpi.esb.datasender.config

case class CliConfig(dataInputPath: Option[String] = None,
                     sendingInterval: Option[Int] = None,
                     verbose: Boolean = false) extends Configurable {

  def isValid: Boolean = (sendingInterval.isEmpty || isValidSendingInterval(sendingInterval)) &&
    (dataInputPath.isEmpty || existsFilePath(dataInputPath.get))
}
