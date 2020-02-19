package org.hpi.esb.commons.util

import scala.collection.JavaConverters._

object ScalaToJavaConverter {

  def list[T](scalaList: List[T]): java.util.List[T] = scalaList.asJava

  def list[T](scalaArray: Array[T]): java.util.List[T] = scalaArray.toList.asJava

}
