package org.hpi.esb.datavalidator.validation

import org.hpi.esb.datavalidator.data.{SimpleRecord, Statistics}

import scala.collection.mutable.ListBuffer

class StatisticsWindow(firstTimestamp: Long, windowSize: Long) extends Window(firstTimestamp, windowSize) {

  var stats = new Statistics()()

  override def update(): Unit = {
    super.update()
    stats = new Statistics()()
  }

  def addValue(value: Long, timestamp: Long): Unit = {
    stats = stats.getUpdatedWithValue(timestamp, value)
  }

  def takeRecords(records: ListBuffer[SimpleRecord]): ListBuffer[SimpleRecord] = {
    val (windowValues, rest) = records.span(r => containsTimestamp(r.timestamp))
    windowValues.foreach(r => addValue(r.value.split("\t")(StatisticsValidation.ColumnIdx).toLong, r.timestamp))
    rest
  }
}
