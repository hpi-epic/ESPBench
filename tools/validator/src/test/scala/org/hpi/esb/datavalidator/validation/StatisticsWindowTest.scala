package org.hpi.esb.datavalidator.validation

import org.hpi.esb.datavalidator.data.Statistics
import org.scalatest.FunSuite

class StatisticsWindowTest extends FunSuite {

  val statisticsWindow = new StatisticsWindow(firstTimestamp = 10, windowSize = 1000)
  val initializedStats = new Statistics()()

  test("testUpdate") {

    assert(statisticsWindow.stats == initializedStats)

    statisticsWindow.addValue(value = 100, timestamp = 100000)
    assert(statisticsWindow.stats != initializedStats)

    statisticsWindow.update()
    assert(statisticsWindow.stats == initializedStats)
  }
}
