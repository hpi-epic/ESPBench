package org.hpi.esb.commons.output.model

abstract class ScenarioResult(l: List[ResultRow]) extends Result {
  override def toTable(): List[List[String]] = {
    val resultRowsLists = l.map(_.toList())
    val header: List[String] = l.head.header
    header :: resultRowsLists
  }
}
