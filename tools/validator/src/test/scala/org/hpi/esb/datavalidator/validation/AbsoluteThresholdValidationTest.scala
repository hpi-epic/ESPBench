package org.hpi.esb.datavalidator.validation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RunnableGraph, Source}
import org.hpi.esb.datavalidator.data.{SimpleRecord, Statistics}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFunSuite, BeforeAndAfter, FunSuite}

trait AbsoluteThresholdValidationTest {
  implicit val system: ActorSystem = ActorSystem("HesseBenchValidator")
  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

  val inTopic = "IN"
  val outTopic = "ABSOLUTE_THRESHOLD"
  val ErrorLimit: Int = AbsoluteThresholdValidation.AbsoluteThreshold

  //(timestamp, value)
  val timestampValuePairs: List[(Long, String)] = List[(Long, String)](
    (1, "col1\tcol2\t1"), (2, "col1\tcol2\t40000"), (3, s"col1\tcol2\t$ErrorLimit"), (4, s"col1\tcol2\t${ErrorLimit + 1}"), (5, "col1\tcol2\t10")
  )

  val correctOutput = List[(Long, String)](
    (1, "col1\tcol2\t40000"),
    (2, s"col1\tcol2\t${ErrorLimit + 1}")
  )

  val incorrectOutput = List[(Long, String)](
    (999, "col1\tcol2\t999"),
    (999, "col1\tcol2\t999")
  )
}

class AbsoluteThresholdValidationTestAsync extends AsyncFunSuite with AbsoluteThresholdValidationTest
  with ValidationTestHelpers with BeforeAndAfter with MockitoSugar {

  test("testCreateSink - correctness and response time fulfilled ") {
    val sink = new AbsoluteThresholdValidation(List(mock[TopicHandler]), mock[TopicHandler], materializer).createSink()

    val goldStandardOutput = createSimpleRecordsList(correctOutput)
    val calculatedOutput = goldStandardOutput

    val testSource = Source(goldStandardOutput.zip(calculatedOutput))

    val graph = combineSourceWithSink[SimpleRecord](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result => assert(result.fulfillsConstraints()) })
  }

  test("testCreateSink - incorrect results") {
    val sink = new AbsoluteThresholdValidation(List(mock[TopicHandler]), mock[TopicHandler], materializer).createSink()

    val goldStandardOutput = createSimpleRecordsList(correctOutput)
    val calculatedOutput = createSimpleRecordsList(incorrectOutput)

    val testSource = Source(goldStandardOutput.zip(calculatedOutput))

    val graph = combineSourceWithSink[SimpleRecord](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result => assert(!result.fulfillsConstraints()) })
  }

  test("testCreateSink - too few output results") {
    val sink = new AbsoluteThresholdValidation(List(mock[TopicHandler]), mock[TopicHandler], materializer).createSink()

    val goldStandardOutput = createSimpleRecordsList(correctOutput)
    val calculatedOutput = createSimpleRecordsList(correctOutput.dropRight(1))

    val testSource = Source(goldStandardOutput.zipAll(calculatedOutput, None, None))

    val graph = combineSourceWithSink[SimpleRecord](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result =>
      assert(!result.fulfillsConstraints())
    })
  }

  test("testCreateSink - too many results") {
    val sink = new AbsoluteThresholdValidation(List(mock[TopicHandler]), mock[TopicHandler], materializer).createSink()

    val goldStandardOutput = createSimpleRecordsList(correctOutput.dropRight(1))
    val calculatedOutput = createSimpleRecordsList(correctOutput)

    val testSource = Source(goldStandardOutput.zipAll(calculatedOutput, None, None))

    val graph = combineSourceWithSink[SimpleRecord](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result =>
      assert(!result.fulfillsConstraints())
    })
  }

  test("testCreateSink - correct response time calculation") {
    val sink = new AbsoluteThresholdValidation(List(mock[TopicHandler]), mock[TopicHandler], materializer).createSink()

    val responseTime = 100
    val goldStandardOutput = createSimpleRecordsList(correctOutput)
    val calculatedOutput = createSimpleRecordsList(correctOutput.map { case (timestamp, value) => (timestamp + responseTime, value) })
    val testSource = Source(goldStandardOutput.zip(calculatedOutput))

    val graph = combineSourceWithSink[SimpleRecord](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    val expectedResponseTimes = Array.fill(correctOutput.length)(responseTime)
    validationResult.map({ result =>
      assert(result.fulfillsConstraints() && result.responseTime.getAllValues.sameElements(expectedResponseTimes))
    })
  }
}
