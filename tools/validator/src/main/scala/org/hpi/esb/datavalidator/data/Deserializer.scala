package org.hpi.esb.datavalidator.data

trait Deserializer[T] {
  def deserialize(value: String, timestamp: Long): T
}
