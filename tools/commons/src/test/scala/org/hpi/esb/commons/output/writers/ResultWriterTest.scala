package org.hpi.esb.commons.output.writers

import java.io.File

import org.mockito.Mockito
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar

import scala.io.Source

class ResultWriterTest extends FunSpec with MockitoSugar {

  describe("ResultWriter") {
    val mockedResultWriter: ResultWriter = mock[ResultWriter]

    it("should filterFilesByPrefix") {

      val prefix = "ESB"
      val firstResultFile = new File(s"${prefix}_results1.csv")
      val secondResultFile = new File(s"${prefix}_results2.csv")
      val otherFile = new File("other.csv")
      val files = List(firstResultFile, secondResultFile, otherFile)
      Mockito.doCallRealMethod().when(mockedResultWriter).filterFilesByPrefix(files, prefix)
      val resultfiles = mockedResultWriter.filterFilesByPrefix(files, prefix)

      assert(resultfiles.toSet.contains(firstResultFile))
      assert(resultfiles.toSet.contains(secondResultFile))
    }

    it("should getIntermediateResultMaps") {
      val csvContent =
        """column1,column2,column3
          |value1,value2,value3""".stripMargin

      val sources = List(Source.fromString(csvContent),
        Source.fromString(csvContent))

      Mockito.doCallRealMethod().when(mockedResultWriter).getIntermediateResultMaps(sources)
      val resultMaps = mockedResultWriter.getIntermediateResultMaps(sources)

      val expectedMap = Map[String, String](
        "column1" -> "value1",
        "column2" -> "value2",
        "column3" -> "value3"
      )
      resultMaps.foreach(r => assert(r.toSet == expectedMap.toSet))
    }
  }
}
