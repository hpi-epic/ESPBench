package org.hpi.esb.util

import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar

import scala.collection.mutable

class TopicManagementTest extends FunSuite with MockitoSugar {

  test("testGetMatchingTopics") {

    val topicsToDelete = mutable.Buffer("ESB_IN_0", "ESB_OUT_O", "ESB_STATISTICS_0")
    val topicsToKeep = mutable.Buffer("esb_new_IN_0", "esb_new_OUT_0", "esb_new_STATISTICS_0", "topic1", "topic2")
    val allTopics = topicsToDelete ++ topicsToKeep
    val prefix =  "ESB_"

    assert(allTopics.containsSlice(topicsToDelete))
    assert(allTopics.containsSlice(topicsToKeep))
    assert(allTopics.size == topicsToDelete.size + topicsToKeep.size)
    assert(TopicManagement.getMatchingTopics(allTopics, prefix) == topicsToDelete)
  }
}
