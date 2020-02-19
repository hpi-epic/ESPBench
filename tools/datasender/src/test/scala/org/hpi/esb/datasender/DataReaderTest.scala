package org.hpi.esb.datasender

import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.io.Source

class DataReaderTest extends FunSpec with Matchers with PrivateMethodTester
  with BeforeAndAfter with MockitoSugar {

  val input1 = "ts id dat00 dat01 dat02"
  val input2 = "ts id dat10 dat11 dat12"
  val source: Source = Source.fromString(input1)
  val source2: Source = Source.fromString(
    s"""$input1
       |$input2""".stripMargin)

  describe("readRecords") {
    val dataReader = new DataReader(source, readInRam = false)
    val dataReaderReadInRam = new DataReader(source, readInRam = true)

    it("should return correct data for 1 line w/o read in ram") {
      assert(dataReader.readRecord.get == input1)
    }

    it("should return correct data for 1 line w/ read in ram") {
      assert(dataReaderReadInRam.readRecord.get == input1)
    }

    it("should return correct data for 2 lines w/o read in ram") {
      val dataReader2 = new DataReader(source2, readInRam = false)
      assert(dataReader2.readRecord.get == input1)
      assert(dataReader2.readRecord.get == input2)
    }

    it("should return correct data for 2 lines w/ read in ram") {
      val source2: Source = Source.fromString(
        s"""$input1
           |$input2""".stripMargin)
      val dataReader2ReadInRam = new DataReader(source2, readInRam = true)
      assert(dataReader2ReadInRam.readRecord.get == input1)
      assert(dataReader2ReadInRam.readRecord.get == input2)
    }

    it("should return correct data for 2 lines w/o read in ram - starts from beginning after reached end (resetIterator test)") {
      val dataReader2 = new DataReader(source2, readInRam = false)
      assert(dataReader2.readRecord.get == input1)
      assert(dataReader2.readRecord.get == input2)
      assert(dataReader2.readRecord.get == input1)
    }

    it("should return correct data for 2 lines w/ read in ram - starts from beginning after reached end (resetIterator test)") {
      val source2: Source = Source.fromString(
        s"""$input1
           |$input2""".stripMargin)
      val dataReader2ReadInRam = new DataReader(source2, readInRam = true)
      assert(dataReader2ReadInRam.readRecord.get == input1)
      assert(dataReader2ReadInRam.readRecord.get == input2)
      assert(dataReader2ReadInRam.readRecord.get == input1)
    }
  }

  describe("retrieveRecord") {

    it("should return next value") {
      val source2: Source = Source.fromString(
        s"""$input1
           |$input2""".stripMargin)
      val dataReader2 = new DataReader(source2, readInRam = false)
      assert(dataReader2.retrieveRecord == input1)
      assert(dataReader2.retrieveRecord == input2)
    }

  }
}
