package org.hpi.esb.datavalidator.validation.graphstage

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.hpi.esb.datavalidator.data.SimpleRecord
import org.scalatest.FunSuite

import scala.collection.immutable


class AccumulateWhileUnchangedTest extends FunSuite {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
  val windowSize = 1000

  def windowStart(timestamp: Long): Long = {
    timestamp - (timestamp % windowSize)
  }

  val numberOfElements = 3000

  test("accumulation of two windows on element stream") {

    val firstWindowElements = immutable.Seq.range(1, 999, 10).map(t => SimpleRecord("1")(t))
    val secondWindowElements = immutable.Seq.range(1000, 1999, 10).map(t => SimpleRecord("1")(t))
    val records = firstWindowElements ++ secondWindowElements

    val s = TestSink.probe[Seq[SimpleRecord]]

    val (_, sink) = Source(records)
      .via(new AccumulateWhileUnchanged(r => windowStart(r.timestamp)))
      .toMat(s)(Keep.both)
      .run()

    sink.request(numberOfElements)
    sink.expectNext(firstWindowElements, secondWindowElements)
    sink.expectComplete()
  }

  test("accumulation on empty stream") {

    val s = TestSink.probe[Seq[SimpleRecord]]

    val records = List[SimpleRecord]()

    val (_, sink) = Source(records)
      .via(new AccumulateWhileUnchanged(r => windowStart(r.timestamp)))
      .toMat(s)(Keep.both)
      .run()

    sink.request(numberOfElements)
    sink.expectComplete()
  }
}
