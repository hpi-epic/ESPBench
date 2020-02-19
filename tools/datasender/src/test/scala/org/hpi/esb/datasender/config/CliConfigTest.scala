package org.hpi.esb.datasender.config

import org.scalatest.FlatSpec

class CliConfigTest extends FlatSpec {

  "CliConfig.isValid" should "return true if none of the options is passed" in {
    val config = CliConfig()
    assert(config.isValid)
  }

  it should "return false if dataInputPath does not exist" in {
    val invalidDataInputPath = Option("hello.txt")
    val config = CliConfig(invalidDataInputPath)
    assert(!config.isValid)
  }

  it should "return false if sending interval is negative" in {
    val invalidSendingInterval = Option(-10)
    val config = CliConfig(sendingInterval = invalidSendingInterval)
    assert(!config.isValid)
  }

}
