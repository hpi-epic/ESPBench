package org.hpi.esb.datasender.config

case class DataReaderConfig(var dataInputPath: List[String],
                            var readInRam: Boolean = false) extends Configurable {

  dataInputPath = {
    dataInputPath.map(_.replaceFirst("~", System.getProperty("user.home")))
  }

  def isValid: Boolean = {
    var pathsValid = true
    dataInputPath.foreach(path => if (!isValidDataInputPath(Some(path))) pathsValid = false)
    pathsValid
  }

}

