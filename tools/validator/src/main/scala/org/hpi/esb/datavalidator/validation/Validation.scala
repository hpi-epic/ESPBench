package org.hpi.esb.datavalidator.validation

import java.io.{BufferedWriter, File, FileWriter}

import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape, Graph, SourceShape}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.data.{Record, SimpleRecord, Statistics}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.metrics.CorrectnessMessages._
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datavalidator.metrics.{CountType, ResultCount}

import scala.concurrent.Future

abstract class Validation[T <: Record](inTopicHandler: List[TopicHandler],
                                       outTopicHandler: TopicHandler,
                                       materializer: ActorMaterializer) extends Logging {

  val valueName: String
  val queryName: String
  var numberOfErrors: Int = 0
  val errorThreshold: Int = 100

  val inNumberOfMessages: Long = {
    var sum: Long = 0
    inTopicHandler.foreach(handler => {
      sum += handler.numberOfMessages
    }
    )
    sum
  }
  val outNumberOfMessages: Long = outTopicHandler.numberOfMessages

  val take: Long => Flow[ConsumerRecord[String, String], ConsumerRecord[String, String], NotUsed]
  = (numberOfMessages: Long) => Flow[ConsumerRecord[String, String]].take(numberOfMessages)

  val toSimpleRecords = Flow[ConsumerRecord[String, String]]
    .map(record => SimpleRecord.deserialize(record.value.split(";").last, record.timestamp))

  val toStatistics = Flow[ConsumerRecord[String, String]]
    .map(record => Statistics.deserialize(record.value, record.timestamp))

  def execute(): Future[QueryValidationState] = {

    val partialSource = createSource()
    val partialSink = createSink()

    val graph = GraphDSL.create(partialSink) { implicit builder =>
      sink =>
        import GraphDSL.Implicits._
        val source = builder.add(partialSource)

        source ~> sink

        ClosedShape
    }

    val runnableGraph = RunnableGraph.fromGraph(graph)
    runnableGraph.run()(materializer)
  }

  def createSink(): Sink[(Option[T], Option[T]), Future[QueryValidationState]] = {

    Sink.fold[QueryValidationState, (Option[T], Option[T])](
      new QueryValidationState(queryName, outTopicHandler.topicName, new ResultCount(inNumberOfMessages))) {
      case (validationResult, pair) => {
        if (numberOfErrors < errorThreshold) {
          updateAndGetValidationState(validationResult, pair)
        } else {
          validationResult
        }
      }
    }
  }

  def updateAndGetValidationState(validationResult: QueryValidationState, pair: (Option[T], Option[T])): QueryValidationState = {
    if (validationResult.query.equals(Configs.QueryNames.MultiThresholdQuery)) {
      //currently not supported
    } else {
      pair match {

        case (Some(expectedValue), (Some(actualValue))) =>
          validationResult.updateResultCount(CountType.Expected)
          validationResult.updateResultCount(CountType.Actual)
          if (expectedValue != actualValue) {
            validationResult.updateResultCount(CountType.Error)
            validationResult.updateCorrectness(isCorrect = false)
            logger.error(unequalValuesMsg(inTopicHandler.map(_.topicName), outTopicHandler.topicName, expectedValue.prettyPrint, actualValue.prettyPrint))
          } else {
            val responseTime = getResponseTime(expectedValue, actualValue)
            validationResult.updateResponseTime(responseTime)
            logResponseTime(responseTime)
          }

        case (None, (Some(_))) =>
          validationResult.updateCorrectness(isCorrect = false)
          validationResult.updateResultCount(CountType.Actual)
          logger.error(tooManyValuedCreatedMsg(inTopicHandler.map(_.topicName), outTopicHandler.topicName, valueName, pair))
          numberOfErrors += 1

        case (Some(_), (None)) =>
          validationResult.updateCorrectness(isCorrect = false)
          validationResult.updateResultCount(CountType.Expected)
          logger.error(tooFewValueCreatedMsg(inTopicHandler.map(_.topicName), outTopicHandler.topicName, valueName, pair))
          numberOfErrors += 1

        case e => logger.error("Unexpected validation result: " + e)
      }
    }
    validationResult
  }

  def logResponseTime(responseTime: Long): Unit = {
    val dir = "logs"
    val file = new File(s"$dir/${outTopicHandler.topicName}_responseTimes.csv")
    if (!file.exists()) {
      if (!new File(dir).exists()) {
        new File(dir).mkdir()
      }
      file.createNewFile()
    }
    val bw = new BufferedWriter(new FileWriter(file, true))
    bw.write(responseTime + "\n")
    bw.close()
  }

  def getResponseTime(inRecord: Record, outRecord: Record): Long = {
    math.max(outRecord.timestamp - inRecord.timestamp, 0)
  }

  def createSource(): Graph[SourceShape[(Option[T], Option[T])], NotUsed]

}
