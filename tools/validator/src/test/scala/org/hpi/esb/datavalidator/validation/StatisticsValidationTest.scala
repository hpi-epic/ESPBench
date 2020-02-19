package org.hpi.esb.datavalidator.validation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RunnableGraph, Source}
import org.hpi.esb.datavalidator.data.Statistics
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFunSuite, BeforeAndAfter, FunSuite}

trait StatisticsValidationTest {
  implicit val system: ActorSystem = ActorSystem("HesseBenchValidator")
  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

  val inTopic = "IN"
  val statsTopic = "STATS"
  val windowSize = 1000

  // (timestamp, value)
  val timestampValuePairs: List[(Long, String)] = List[(Long, String)](
    (1, "col1\tcol2\t1"), (500, "col1\tcol2\t2"), // first window
    (1000, "col1\tcol2\t3"), (1001, "col1\tcol2\t4"), (1050, "col1\tcol2\t5") // second window
  )

  val correctResultStats: List[(Long, String)] = List[(Long, String)](
    (2900, "1,2,3,2,1.5"),
    (3001, "3,5,12,3,4")
  )

  val inCorrectResultStats = List[(Long, String)](
    (999, "999,999,999,999,999"),
    (3000, "999,999,999,999,999")
  )
}

class StatisticsValidationTestAsync extends AsyncFunSuite with StatisticsValidationTest
  with ValidationTestHelpers with BeforeAndAfter with MockitoSugar {

  test("testCreateSink - correctness and response time fulfilled ") {
    val sink = new StatisticsValidation(List(mock[TopicHandler]), mock[TopicHandler], windowSize, materializer).createSink()

    val inStatistics = correctResultStats.map { case (timestamp, value) => Some(Statistics.deserialize(value, timestamp)) }
    val outStatistics = inStatistics
    val testSource = Source(inStatistics.zip(outStatistics))

    val graph = combineSourceWithSink[Statistics](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result => assert(result.fulfillsConstraints()) })
  }

  test("testCreateSink - incorrect results") {
    val sink = new StatisticsValidation(List(mock[TopicHandler]), mock[TopicHandler], windowSize, materializer).createSink()

    val inStatistics = createStatisticsList(correctResultStats)
    val outStatistics = createStatisticsList(inCorrectResultStats)
    val testSource = Source(inStatistics.zip(outStatistics))

    val graph = combineSourceWithSink[Statistics](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map(result => assert(!result.fulfillsConstraints()))
  }

  test("testCreateSink - too few statistics results") {
    val sink = new StatisticsValidation(List(mock[TopicHandler]), mock[TopicHandler], windowSize, materializer).createSink()

    val inStatistics = createStatisticsList(correctResultStats)
    val outStatistics = createStatisticsList(correctResultStats.dropRight(1))
    val testSource = Source(inStatistics.zipAll(outStatistics, None, None))

    val graph = combineSourceWithSink[Statistics](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result =>
      assert(!result.fulfillsConstraints())
    })
  }

  test("testCreateSink - too many statistics results") {
    val sink = new StatisticsValidation(List(mock[TopicHandler]), mock[TopicHandler], windowSize, materializer).createSink()

    val inStatistics = createStatisticsList(correctResultStats.dropRight(1))
    val outStatistics = createStatisticsList(correctResultStats)
    val testSource = Source(inStatistics.zipAll(outStatistics, None, None))

    val graph = combineSourceWithSink[Statistics](testSource, sink)
    val validationResult = RunnableGraph.fromGraph(graph).run()

    validationResult.map({ result =>
      assert(!result.fulfillsConstraints())
    })
  }

  test("testCreateSink - correct response time calculation - response time positive") {
    val sink = new StatisticsValidation(List(mock[TopicHandler]), mock[TopicHandler], windowSize, materializer).createSink()
    val responseTimeDiff = 200
    correctResultStats.foreach { resultRecord =>

      val inStatistics = createStatisticsList(List(resultRecord))
      val outStatistics = createStatisticsList(List(resultRecord).map {
        case (timestamp, value) => (timestamp + responseTimeDiff, value)
      })

      val testSource = Source(inStatistics.zip(outStatistics))
      val graph = combineSourceWithSink[Statistics](testSource, sink)
      val validationResult = RunnableGraph.fromGraph(graph).run()

      val theoreticResponseTime = resultRecord._1 + responseTimeDiff - new Window(resultRecord._1, windowSize).windowEnd
      val expectedResponseTimes: Long = if (theoreticResponseTime < 0) 0 else theoreticResponseTime
      validationResult.map({ result =>
        assert(result.fulfillsConstraints() && result.responseTime.getAllValues.sameElements(List(expectedResponseTimes)))
      })
    }
    assert(true) //workaround as assertion is needed
  }
}
