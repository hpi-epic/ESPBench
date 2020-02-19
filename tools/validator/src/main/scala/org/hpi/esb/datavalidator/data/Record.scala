package org.hpi.esb.datavalidator.data

abstract class Record(val timestamp: Long) {
  def prettyPrint: String
}
