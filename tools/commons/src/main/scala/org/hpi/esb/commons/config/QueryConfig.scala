package org.hpi.esb.commons.config

import pureconfig.generic.auto._
import org.hpi.esb.commons.util.Logging

case class QueryConfig(name: String, numberOfStreams: Int) extends Logging {

  def areValidColumns(): Boolean = {
    def valid = name.nonEmpty && numberOfStreams > 0

    if (!valid) {
      logger.error(s"Query name must not be empty (is: $name) and number of streams must be greater than zero (is: $numberOfStreams).")
    }
    valid
  }
}
