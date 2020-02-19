package org.hpi.esb.commons.output.model

abstract class Result {
  def toTable(): List[List[String]]
}
