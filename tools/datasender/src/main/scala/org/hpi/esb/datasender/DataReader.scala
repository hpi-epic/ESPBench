package org.hpi.esb.datasender

import org.hpi.esb.commons.util.Logging

import scala.io.Source

class DataReader(val source: Source, readInRam: Boolean, continueFromBeginningWhenFinished: Boolean = true)
  extends Logging {

  private var dataIterator: Iterator[String] = if (readInRam) recordList.toIterator else source.getLines
  private lazy val recordList = source.getLines.toList

  def readRecord: Option[String] = {
    if (dataIterator.hasNext) {
      Some(retrieveRecord)
    } else {
      if (continueFromBeginningWhenFinished) {
        resetIterator()
        Some(retrieveRecord)
      } else {
        Option.empty[String]
      }
    }
  }

  def retrieveRecord: String = {
    dataIterator.next()
  }

  def resetIterator(): Unit = {
    if (readInRam) {
      dataIterator = recordList.toIterator
    } else {
      dataIterator = source.reset().getLines()
    }
  }

  def close(): Unit = source.close
}
