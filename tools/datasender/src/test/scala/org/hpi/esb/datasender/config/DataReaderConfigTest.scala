package org.hpi.esb.datasender.config

import org.apache.log4j.Logger
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class DataReaderConfigTest extends FlatSpec with Matchers with MockitoSugar {

  val columnsIndices: Option[List[Int]] = Option(List(1, 3))
  val columnDelimiter: Option[String] = Option("\\s+")
  val dataInputPath: List[String] = List[String]("~/hello.world")

  "DataReaderConfig.isValid" should "return false if 'dataInputPath' is empty" in {
    val invalidDataInputPath = List[String]("")
    val config = DataReaderConfig(invalidDataInputPath)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if 'dataInputPath' is not a valid file path" in {
    val invalidDataInputPath = List[String]("non_existant_file.txt")
    val config = DataReaderConfig(invalidDataInputPath)
    config.logger = mock[Logger]

    assert(!config.isValid)
    verify(config.logger, times(1)).error(org.mockito.ArgumentMatchers.any[String])
  }

  it should "return false if 'dataInputPath' starts with tilde" in {
    val inputPath = List[String]("~/test")
    val config = DataReaderConfig(inputPath)
    assert(!config.dataInputPath.startsWith("~"))
  }

  it should "return true if everything is fine" in {
    val config = DataReaderConfig(dataInputPath)
    config.logger = mock[Logger]
    val mockedConfig = Mockito.spy(config)
    Mockito.doReturn(true, Nil: _*).when(mockedConfig).isValidDataInputPath(Some(config.dataInputPath.head))

    assert(mockedConfig.isValid)
    verify(mockedConfig.logger, times(0)).error(org.mockito.ArgumentMatchers.any[String])
  }
}
