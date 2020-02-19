package org.hpi.esb.commons.output.model

abstract class ResultRow extends Result {
  val header: List[String]
  def toList(): List[String]
  def toTable(): List[List[String]]
}
