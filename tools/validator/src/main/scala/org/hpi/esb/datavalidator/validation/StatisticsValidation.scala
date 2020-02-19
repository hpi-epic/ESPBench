package org.hpi.esb.datavalidator.validation

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL}
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.configuration.Config
import org.hpi.esb.datavalidator.data.{Record, SimpleRecord, Statistics}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.validation.graphstage.{AccumulateWhileUnchanged, IgnoreLastElements, ZipWhileEitherAvailable}

object StatisticsValidation {
  val ColumnIdx = 2
}

class StatisticsValidation(inTopicHandler: List[TopicHandler],
                           outTopicHandler: TopicHandler,
                           windowSize: Long,
                           materializer: ActorMaterializer)
  extends Validation[Statistics](inTopicHandler, outTopicHandler, materializer) {

  override val valueName = "Statistics"
  override val queryName = Configs.QueryNames.StatisticsQuery

  val collectByWindow = new AccumulateWhileUnchanged[SimpleRecord, Long](r => windowStart(r.timestamp))
  val calculateStatistics = Flow[Seq[SimpleRecord]].map(s =>
    s.foldLeft(new Statistics()())((stats, record) =>
      stats.getUpdatedWithValue(record.timestamp, record.value.split("\t")(StatisticsValidation.ColumnIdx).toLong)))

  override def getResponseTime(inRecord: Record, outRecord: Record): Long = {
    var responseTime = outRecord.timestamp - new Window(inRecord.timestamp, windowSize).windowEnd
    if (responseTime < 0) {
      responseTime = 0
    }
    responseTime
  }

  def createSource(): Graph[SourceShape[(Option[Statistics], Option[Statistics])], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(ZipWhileEitherAvailable[Statistics]())
      val ignoreLastTwoElements = builder.add(new IgnoreLastElements[(Option[Statistics], Option[Statistics])](ignoreCount = 1))

      inTopicHandler.head.topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> collectByWindow ~> calculateStatistics ~> zip.in0
      outTopicHandler.topicSource ~> take(outNumberOfMessages) ~> toStatistics ~> zip.in1
      zip.out ~> ignoreLastTwoElements

      SourceShape(ignoreLastTwoElements.out)
    }
  }

  def windowStart(timestamp: Long): Long = {
    timestamp - (timestamp % windowSize)
  }
}
