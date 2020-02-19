package org.hpi.esb.datavalidator

import org.hpi.esb.commons.util.Logging

object Main {
  def main(args: Array[String]): Unit = {
    Logging.setToInfo()
    val validator = new Validator()
    validator.execute()
  }
}
