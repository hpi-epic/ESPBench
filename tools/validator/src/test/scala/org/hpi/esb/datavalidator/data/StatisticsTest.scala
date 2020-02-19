package org.hpi.esb.datavalidator.data

import org.scalatest.FunSuite

class StatisticsTest extends FunSuite {

  test("testConstructor") {
    val stats = new Statistics()()
    assert(stats.min === Long.MaxValue)
    assert(stats.max === Long.MinValue)
    assert(stats.avg === 0)
    assert(stats.sum === 0)
    assert(stats.count === 0)
  }

  test("testGetUpdatedWithValue - update with single value") {
    val stats = new Statistics()()
    val newValue = 10
    val newTimestamp = 10
    val newStats = stats.getUpdatedWithValue(newTimestamp, newValue)

    assert(newStats.min == newValue)
    assert(newStats.max == newValue)
    assert(newStats.avg == newValue)
    assert(newStats.count == 1)
    assert(newStats.sum == newValue)
  }

  test("testGetUpdatedWithValue - update with multiple values") {
    val stats = new Statistics()()
    val values = Seq(1,2,3,8,9)

    val newStats = values.zipWithIndex.foldLeft(stats) {
      case (s, (value, timestamp)) => s.getUpdatedWithValue(timestamp, value)
    }

    assert(newStats.min === 1)
    assert(newStats.max === 9)
    assert(newStats.avg === 4.6)
    assert(newStats.sum === 23)
    assert(newStats.count === 5)
    assert(newStats.timestamp === 4)
  }

  test("testPrettyPrint") {
    val stats = new Statistics(min = 2, max = 4, sum = 6, count = 2, avg = 3)(timestamp = 4)
    assert(stats.prettyPrint == "Min: 2, Max: 4, Sum: 6, Count: 2, Avg: 3.0, LatestTimestamp: 4")
  }

  test("testToString") {
    val stats = new Statistics(min = 2, max = 4, sum = 6, count = 2, avg = 3)(timestamp = 4)
    assert(stats.toString == "2,4,6,2,3.0,4")
  }

  test("testCreate - Successful") {
    val stats = Statistics.deserialize(stats = "2,4,6,2,3.0", timestamp = 4)

    assert(stats.min === 2)
    assert(stats.max === 4)
    assert(stats.avg === 3.0)
    assert(stats.sum === 6)
    assert(stats.count === 2)
  }

  test("testCreate - Unsuccessful") {
    assertThrows[IllegalArgumentException] {
      Statistics.deserialize(stats = "incorrect string", timestamp = 4)
    }
  }
}
