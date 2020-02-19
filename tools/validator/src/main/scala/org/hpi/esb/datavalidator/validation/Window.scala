package org.hpi.esb.datavalidator.validation

class Window(firstTimestamp: Long, val windowSize: Long) {

  var windowEnd: Long = (firstTimestamp - (firstTimestamp % windowSize)) + windowSize

  def containsTimestamp(t: Long): Boolean = {
    t < windowEnd
  }

  def update(): Unit = {
    windowEnd += windowSize
  }
}
