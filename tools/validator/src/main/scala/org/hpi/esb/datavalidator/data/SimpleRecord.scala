package org.hpi.esb.datavalidator.data

object SimpleRecord extends Deserializer[SimpleRecord] {
  override def deserialize(value: String, timestamp: Long): SimpleRecord = {
    try {
      SimpleRecord(value)(timestamp)
    } catch {
      case _: Exception => throw new IllegalArgumentException(s"The string '$value' could not be deserialized into a SimpleRecord.")
    }
  }
}

case class SimpleRecord(value: String)(override val timestamp: Long = 0) extends Record(timestamp) {
  override def prettyPrint: String = s"Value: $value; Timestamp: $timestamp "
}