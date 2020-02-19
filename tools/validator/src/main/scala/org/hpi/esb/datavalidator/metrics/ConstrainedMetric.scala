package org.hpi.esb.datavalidator.metrics

trait ConstrainedMetric {
  def fulfillsConstraint: Boolean
}
