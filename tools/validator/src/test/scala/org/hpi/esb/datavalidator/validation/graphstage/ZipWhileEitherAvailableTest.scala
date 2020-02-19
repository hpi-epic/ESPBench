package org.hpi.esb.datavalidator.validation.graphstage

import akka.actor.ActorSystem
import akka.stream.scaladsl.{GraphDSL, RunnableGraph}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.stream.testkit.{TestPublisher, TestSubscriber}
import akka.stream.{ActorMaterializer, ClosedShape, Graph}
import org.scalatest.FunSuite

class ZipWhileEitherAvailableTest extends FunSuite {


  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)


  def createGraph(): Graph[ClosedShape.type, (TestPublisher.Probe[Int], TestPublisher.Probe[Int], TestSubscriber.Probe[(Option[Int], Option[Int])])] = {

    val s1 = TestSource.probe[Int]
    val s2 = TestSource.probe[Int]
    val sink = TestSink.probe[(Option[Int], Option[Int])]
    GraphDSL.create(s1, s2, sink)((_, _, _)) { implicit builder =>
      (source1, source2, s) =>
        import GraphDSL.Implicits._

        val zip = builder.add(new ZipWhileEitherAvailable[Int, Int, (Option[Int], Option[Int])](Tuple2.apply[Option[Int], Option[Int]]))

        source1 ~> zip.in0
        source2 ~> zip.in1

        zip.out ~> s
        ClosedShape
    }
  }

  test("equal number of 'in1' and 'in2' messages") {

    val in1 = List.range(0, 3)
    val in2 = List.range(0, 3)
    val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
    sink.request(3)

    in1.foreach(source1.sendNext)
    source1.sendComplete()

    in2.foreach(source2.sendNext)
    source2.sendComplete()

    in1.zip(in2).foreach {
      case (v1, v2) => sink.expectNext((Some(v1), Some(v2)))
    }
    sink.expectComplete()
  }

  test("more messages in 'in1'") {

    val in1 = List.range(0, 3)
    val in2 = List.range(0, 1)
    val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
    sink.request(3)

    in1.foreach(source1.sendNext)
    source1.sendComplete()

    in2.foreach(source2.sendNext)
    source2.sendComplete()

    sink.expectNext((Some(0), Some(0)))
    sink.expectNext((Some(1), None))
    sink.expectNext((Some(2), None))
    sink.expectComplete()
  }

    test("more messages in 'in2'") {

      val in1 = List.range(0, 1)
      val in2 = List.range(0, 3)
      val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
      sink.request(3)

      in1.foreach(source1.sendNext)
      source1.sendComplete()

      in2.foreach(source2.sendNext)
      source2.sendComplete()

      sink.expectNext((Some(0), Some(0)))
      sink.expectNext((None, Some(1)))
      sink.expectNext((None, Some(2)))
      sink.expectComplete()
    }

    test("empty 'in1'") {

      val in1 = List.range(0, 3)
      val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
      sink.request(3)

      source2.sendComplete()
      in1.foreach(source1.sendNext)
      source1.sendComplete()

      sink.expectNext((Some(0), None))
      sink.expectNext((Some(1), None))
      sink.expectNext((Some(2), None))
      sink.expectComplete()
    }

    test("empty 'in2'") {

      val in2 = List.range(0, 3)
      val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
      sink.request(3)

      source1.sendComplete()
      in2.foreach(source2.sendNext)
      source2.sendComplete()

      sink.expectNext((None, Some(0)))
      sink.expectNext((None, Some(1)))
      sink.expectNext((None, Some(2)))
      sink.expectComplete()
    }

  test("empty 'in1' and empty 'in2'") {

    val (source1, source2, sink) = RunnableGraph.fromGraph(createGraph()).run()
    sink.request(3)

    source1.sendComplete()
    source2.sendComplete()
    sink.expectComplete()
  }
}
