package org.hpi.esb.datavalidator.validation

import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, ZipWith}
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.data.SimpleRecord
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.validation.graphstage.ZipWhileEitherAvailable

class MultiThresholdValidation(inTopicHandler: List[TopicHandler],
                               outTopicHandler: TopicHandler,
                               materializer: ActorMaterializer)
  extends Validation[SimpleRecord](inTopicHandler, outTopicHandler, materializer) {
  override val valueName: String = "(Record1,Record2)"
  override val queryName: String = Configs.QueryNames.MultiThresholdQuery

  val zip = ZipWith[SimpleRecord, SimpleRecord, (SimpleRecord, SimpleRecord)](
    (inp1, inp2) => (inp1, inp2)
  )

  override def createSource(): Graph[SourceShape[(Option[SimpleRecord], Option[SimpleRecord])], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(ZipWhileEitherAvailable[SimpleRecord]())

      inTopicHandler(1).topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> zip.in0
      inTopicHandler.head.topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> zip.in1

      SourceShape(zip.out)
    }
  }
}
