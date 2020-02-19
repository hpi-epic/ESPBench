package org.hpi.esb.datasender

object TestHelper {
  def checkEquality[S, T](m1: Map[S, T], m2: Map[S, T]): Unit = {
    m1.foreach {
      case (key, value) => {
        val expectedValue = m2(key)
        assert(value == expectedValue)
      }
    }
  }
}
