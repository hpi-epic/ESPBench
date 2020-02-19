package org.hpi.esb.datavalidator.output.model

import org.hpi.esb.commons.config.Configs.BenchmarkConfig

object ConfigValues {
  val SENDING_INTERVAL = "sendingInterval"
  val SENDING_INTERVAL_TIMEUNIT = "sendingIntervalTimeUnit"

  val header = List(SENDING_INTERVAL, SENDING_INTERVAL_TIMEUNIT)

  def get(benchmarkConfig: BenchmarkConfig): ConfigValues = {
    ConfigValues(benchmarkConfig.sendingInterval.toString,
      benchmarkConfig.sendingIntervalTimeUnit)
  }
}

import org.hpi.esb.datavalidator.output.model.ConfigValues._

case class ConfigValues(sendingInterval: String, sendingIntervalUnit: String) {

  def this(m: Map[String, String]) = this(m(SENDING_INTERVAL), m(SENDING_INTERVAL_TIMEUNIT))

  def toList(): List[String] = {
    List(sendingInterval.toString, sendingIntervalUnit)
  }
}

