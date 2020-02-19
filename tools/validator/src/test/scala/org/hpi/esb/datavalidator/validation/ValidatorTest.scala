package org.hpi.esb.datavalidator.validation

import org.apache.log4j.Logger
import org.hpi.esb.commons.config.Configs.InternalQueryConfig
import org.hpi.esb.commons.config.{Configs, QueryConfig}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.Validator
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.immutable.HashMap

class ValidatorTest extends FunSuite with BeforeAndAfter with MockitoSugar {

  val mockedTopicHandler: TopicHandler = mock[TopicHandler]
  val spy: TopicHandler = Mockito.spy(mockedTopicHandler)
  Mockito.doReturn(1L, Nil: _*).when(spy).numberOfMessages

  test("getValidationsSupportedQueries") {
    val queries = List[String](Configs.QueryNames.AbsoluteThresholdQuery, Configs.QueryNames.IdentityQuery)
    val queryConfigs: List[InternalQueryConfig] = queries.map(query =>
      InternalQueryConfig(query, Configs.benchmarkConfig.getSourceNameList(query, 1), Configs.benchmarkConfig.getSinkName(query))
    )
    var topicHandlerMap: HashMap[String, TopicHandler] = HashMap[String, TopicHandler]()
    queryConfigs.foreach(config =>
      topicHandlerMap += (config.inputTopics.head -> mockedTopicHandler, config.outputTopic -> mockedTopicHandler)
    )
    val validationsList = new Validator().getValidations(queryConfigs, topicHandlerMap)
    assert(validationsList.lengthCompare(queries.length) == 0)
  }

  test("getValidationsNotSupportedQueryInConfig") {
    val notSupportedQuery = "FCMagdeburgIsNotASupportedQueryYet"
    val queries = List[String](notSupportedQuery, Configs.QueryNames.AbsoluteThresholdQuery, Configs.QueryNames.IdentityQuery)
    val queryConfigs: List[InternalQueryConfig] = queries.map(query =>
      InternalQueryConfig(query, Configs.benchmarkConfig.getSourceNameList(query, 1), Configs.benchmarkConfig.getSinkName(query))
    )
    var topicHandlerMap: HashMap[String, TopicHandler] = HashMap[String, TopicHandler]()
    queryConfigs.foreach(config =>
      topicHandlerMap += (config.inputTopics.head -> mockedTopicHandler, config.outputTopic -> mockedTopicHandler)
    )

    val validator = new Validator()
    val mockedLogger = mock[Logger]
    validator.logger = mockedLogger
    val validationsList = validator.getValidations(queryConfigs, topicHandlerMap)
    assert(validationsList.lengthCompare(queries.length - 1) == 0)
    verify(validator.logger, times(1)).warn(s"Query $notSupportedQuery is currently not supported.")
  }

}
