package org.hpi.esb.commons.util

import org.apache.log4j.{Level, Logger}

trait Logging {
  var logger: Logger = Logger.getLogger("HesseBenchLogger")
}

object Logging {

  def setToInfo() {
    Logger.getRootLogger.setLevel(Level.INFO)
  }

  def setToDebug() {
    Logger.getRootLogger.setLevel(Level.DEBUG)
  }
}
