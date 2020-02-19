package org.hpi.esb.datavalidator.output.model

import org.hpi.esb.commons.output.model.ResultRow

case class ValidatorResultRow(configValues: ConfigValues, resultValues: ResultValues)
  extends ResultRow {

  override val header: List[String] = ConfigValues.header ++ ResultValues.header

  def this(m: Map[String, String]) = this(new ConfigValues(m), new ResultValues(m))

  override def toList(): List[String] = {
    configValues.toList() ++ resultValues.toList()
  }

  override def toTable(): List[List[String]] = {
    List(header, configValues.toList() ++ resultValues.toList())
  }
}

object ValidatorResultRow {
  val header: List[String] = ConfigValues.header ++ ResultValues.header
}
