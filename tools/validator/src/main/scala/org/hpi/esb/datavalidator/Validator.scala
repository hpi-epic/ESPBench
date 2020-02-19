package org.hpi.esb.datavalidator

import java.io.{PrintWriter, StringWriter}

import org.hpi.esb.commons.config.Configs.QueryNames._
import org.hpi.esb.commons.config.Configs.{InternalQueryConfig, QueryNames, benchmarkConfig}
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datavalidator.configuration.Config.validatorConfig
import org.hpi.esb.datavalidator.data.Record
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.output.writers.ValidatorRunResultWriter
import org.hpi.esb.datavalidator.validation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class Validator() extends Logging {

  val startTime: Long = currentTimeInSecs

  def execute(): Unit = {
    logger.info(s"Start time: $startTime")

    val allTopics = benchmarkConfig.topics
    val queryConfigs = benchmarkConfig.fullQueryConfigs

    val topicHandlersByName = allTopics.map(topic =>
      topic -> TopicHandler.create(topic, AkkaManager.system)
    ).toMap

    val validationResults = getValidations(queryConfigs, topicHandlersByName)
      .map(_.execute())

    val resultWriter = new ValidatorRunResultWriter()

    validationResults.foreach(future => future.onComplete {
      case Success(results) =>
        logger.info(s"Finished ${results.query}")
        results.validationFinishedTs = currentTimeInSecs
      case Failure(e) =>
        logError(e)
    })

    Future.sequence(validationResults).onComplete({
      case Success(results) =>
        resultWriter.outputResults(results, startTime)
        terminate()
      case Failure(e) =>
        logError(e)
        terminate()
    })
  }

  private def logError(exception: Throwable): Unit = {
    logger.error("Error during validation:\n")
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    logger.error(sw.toString)
  }

  def terminate(): Unit = {
    AkkaManager.terminate()
  }

  def getValidations(queryConfigs: List[InternalQueryConfig], topicHandlersByName: Map[String, TopicHandler]): List[Validation[_ <: Record]] = {
    var validationList: List[Validation[_ <: Record]] = List[Validation[_ <: Record]]()
    queryConfigs.foreach { config: InternalQueryConfig =>

      var topicList: List[TopicHandler] = List[TopicHandler]()
      config.inputTopics.foreach(topic => {
        if (topicHandlersByName.contains(topic)) {
          topicList = topicList ++ List(topicHandlersByName(topic))
        }
      })

      config match {
        case InternalQueryConfig(IdentityQuery, config.inputTopics, config.outputTopic) =>
          validationList ::= new IdentityValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(StatisticsQuery, config.inputTopics, config.outputTopic) =>
          validationList ::= new StatisticsValidation(topicList, topicHandlersByName(config.outputTopic), validatorConfig.windowSize, AkkaManager.materializer)

        case InternalQueryConfig(AbsoluteThresholdQuery, config.inputTopics, config.outputTopic) =>
          validationList ::= new AbsoluteThresholdValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(MultiThresholdQuery, config.inputTopics, config.outputTopic) =>
          logger.warn(s"Query $MultiThresholdQuery is currently not supported.")
        //validationList ::= new MultiThresholdValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(MachinePowerQuery, config.inputTopics, config.outputTopic) =>
          validationList ::= new MachinePowerValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(ProcessingTimesQuery, config.inputTopics, config.outputTopic) =>
          validationList ::= new ProcessingTimesValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(QueryNames.StochasticOutlierSelection, config.inputTopics, config.outputTopic) =>
          validationList ::= new StochasticOutlierSelectionValidation(topicList, topicHandlersByName(config.outputTopic), AkkaManager.materializer)

        case InternalQueryConfig(queryName, config.inputTopics, config.outputTopic) =>
          logger.warn(s"Query $queryName is currently not supported.")

        case other =>
          logger.error(s"Unexpected query config: $other")
      }
    }
    validationList
  }

  def currentTimeInSecs: Long = System.currentTimeMillis / 1000
}
