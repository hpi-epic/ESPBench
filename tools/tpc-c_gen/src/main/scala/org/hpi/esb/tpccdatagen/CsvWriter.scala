package org.hpi.esb.tpccdatagen

import java.io.{BufferedOutputStream, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.Date

import org.hpi.esb.tpccdatagen.table.Record

class CsvWriter(tableFile: String) {

  val dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  var bufferedOutputStream: BufferedOutputStream = new BufferedOutputStream(
    new FileOutputStream(tableFile, true)
  )
  val stringBuilder: StringBuilder = new StringBuilder()

  def addRecord(record: Record): Unit = {
    stringBuilder.setLength(0)
    val columnArray = record.getColumnArray
    for (index <- columnArray.indices) {
      if (index > 0) {
        stringBuilder.append("\t")
      }
      if (columnArray(index) == null) {
        stringBuilder.append("\\N")
      } else {
        columnArray(index) match {
          case date: Date =>
            stringBuilder.append(dateTimeFormat.format(date))
          case _ =>
            stringBuilder.append(columnArray(index))
        }
      }
    }
    bufferedOutputStream.write(stringBuilder.toString().getBytes())
    bufferedOutputStream.write("\n".getBytes())
  }

  def close(): Unit = bufferedOutputStream.close()
}
