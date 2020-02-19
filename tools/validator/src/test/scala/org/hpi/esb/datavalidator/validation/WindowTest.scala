package org.hpi.esb.datavalidator.validation

import org.scalatest.{BeforeAndAfter, FunSuite}

class WindowTest extends FunSuite with BeforeAndAfter {

  val windowSize = 1000

  test("testSetInitialWindowEnd") {

    var timestamps = List(0, 1, 100, 999)

    timestamps.foreach(t => {
      val window = new Window(firstTimestamp = t, windowSize)
      assert(window.windowEnd == 1000)
    })

    timestamps = List(1000, 1001)

    timestamps.foreach(t => {
      val window = new Window(firstTimestamp = t, windowSize)
      assert(window.windowEnd == 2000)
    })
  }

  test("testContainsTimestamp") {

    val window = new Window(firstTimestamp = 1, windowSize)

    val containedValues = List(0,1,100,999)
    assert(containedValues.forall(window.containsTimestamp(_)))

    val notContainedValues = List(1000, 1001)
    assert(notContainedValues.forall(!window.containsTimestamp(_)))
  }

  test("testUpdateWindowEnd") {

    val window = new Window(firstTimestamp = 1, windowSize)
    window.update()

    assert(window.windowEnd == 2000)
  }
}
