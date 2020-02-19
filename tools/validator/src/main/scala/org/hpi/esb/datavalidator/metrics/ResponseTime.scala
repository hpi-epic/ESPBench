package org.hpi.esb.datavalidator.metrics

import com.codahale.metrics.{Histogram, Snapshot, UniformReservoir}
import org.hpi.esb.commons.output.Util._
import org.hpi.esb.datavalidator.output.model.ResultValues

object ResponseTime {
  // in milliseconds
  val referenceValue: Long = 2000
  val percentile: Double = 0.90
  val precision = 2
  val header = List(ResultValues.RT_FULFILLED, ResultValues.PERCENTILE_COLUMN, "RT-Min in ms", "RT-Max in ms", "RT-Mean in ms")

  def fulfillsConstraint(value: Double): Boolean = {
    value < ResponseTime.referenceValue
  }
}

class ResponseTime extends BenchmarkResult with ConstrainedMetric {

  private lazy val snapshot: Snapshot = histogram.getSnapshot
  val histogram = new Histogram(new UniformReservoir())

  def getGeneralInfo: String = s"Min: $getMin; Max: $getMax; Mean: $getMin"

  def getPercentile: Int = {
    (ResponseTime.percentile * 100).toInt
  }

  def updateValue(value: Long): Unit = {
    histogram.update(value)
  }

  def getAllValues: Array[Long] = {
    snapshot.getValues
  }

  override def getMeasuredResults: List[String] =
    List(fulfillsConstraint.toString, getPercentileValue.toString, getMin.toString, getMax.toString, getMean.toString)

  def getMin: Double = round(snapshot.getMin, ResponseTime.precision)

  def getMax: Double = round(snapshot.getMax, ResponseTime.precision)

  override def fulfillsConstraint: Boolean = {
    ResponseTime.fulfillsConstraint(getPercentileValue)
  }


  def getPercentileValue: Double = {
    round(snapshot.getValue(ResponseTime.percentile), ResponseTime.precision)
  }

  def getMean: Double = round(snapshot.getMean, ResponseTime.precision)

  override def getResultsHeader: List[String] = ResponseTime.header
}
