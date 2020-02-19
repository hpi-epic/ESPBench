package org.hpi.esb.tpccdatagen.table

class Record(numberOfColumns: Int) {

  private val columnArray: Array[Any] = Array.ofDim(numberOfColumns)

  private var nextColumnIndex: Int = 0

  def reset(): Unit = nextColumnIndex = 0
  def add(value: Any): Unit = {
    columnArray(nextColumnIndex) = value
    nextColumnIndex += 1
  }

  def getColumnArray: Array[Any] = columnArray
}
