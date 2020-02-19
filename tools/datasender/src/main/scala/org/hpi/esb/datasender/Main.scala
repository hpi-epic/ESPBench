package org.hpi.esb.datasender

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.config.ConfigHandler
import org.hpi.esb.datasender.db.ImportData

object Main extends Logging {

  def main(args: Array[String]): Unit = {
    logger.info("args: " + args.mkString(" "))
    logger.info("Starting Datasender...")
    if (args.length > 0 && args(0).contains("import")) {
      new ImportData().importErpData
    } else {
      setLogLevel(ConfigHandler.config.verbose)
      new DataDriver().run()
    }
  }

  def setLogLevel(verbose: Boolean): Unit = {
    if (verbose) {
      Logging.setToDebug
      logger.info("DEBUG/VERBOSE mode switched on")
    }
  }
}
