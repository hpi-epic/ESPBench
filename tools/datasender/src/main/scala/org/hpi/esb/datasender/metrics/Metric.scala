package org.hpi.esb.datasender.metrics

abstract class Metric {
  def getMetrics(): Map[String, String]
}
