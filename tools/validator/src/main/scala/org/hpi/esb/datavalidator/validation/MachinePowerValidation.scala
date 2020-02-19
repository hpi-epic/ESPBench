package org.hpi.esb.datavalidator.validation

import java.sql.Timestamp
import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Source}
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.AkkaManager
import org.hpi.esb.datavalidator.data.SimpleRecord
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.validation.graphstage.ZipWhileEitherAvailable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object MachinePowerValidation {
  val Threshold = 8105
  val ColumnIdx: Int = 4
  val session = Database.forConfig("postgres")
  AkkaManager.system.registerOnTermination(session.close())
}

class MachinePowerValidation(inTopicHandler: List[TopicHandler],
                             outTopicHandler: TopicHandler,
                             materializer: ActorMaterializer)
  extends Validation[SimpleRecord](inTopicHandler, outTopicHandler, materializer) {

  override val valueName: String = "MachinePowerAlert Records"
  override val queryName: String = Configs.QueryNames.MachinePowerQuery

  val filterThreshold: Flow[SimpleRecord, SimpleRecord, NotUsed] = {
    Flow[SimpleRecord].filter(record => {
      record.value.split("\t")(MachinePowerValidation.ColumnIdx).toLong < MachinePowerValidation.Threshold
    })
  }

  val filterDowntime: Flow[SimpleRecord, SimpleRecord, NotUsed] = Flow[SimpleRecord].filter(
    sensorRecord => {
      val recordAsList = sensorRecord.value.split("\t")
      val workplace = recordAsList(recordAsList.length - 1).toInt

      try {
        val query =
          sql"""SELECT "WP_DOWNTIME_START", "WP_DOWNTIME_END" FROM workplace WHERE "WP_ID" = $workplace""".as[(Timestamp, Timestamp)]
        val res = Await.result(MachinePowerValidation.session.run(query), 10.seconds).toList
        val timestamp: Instant = Instant.ofEpochMilli(sensorRecord.timestamp)
        if (timestamp.isAfter(res.head._1.toInstant) &&
          timestamp.isBefore(res.head._2.toInstant)) {
          false
        } else {
          true
        }
      } catch {
        case e: Throwable =>
          logger.error(s"DB exception in $queryName validation: ${e.printStackTrace()}")
          false
      }
    }
  )

  override def createSource(): Graph[SourceShape[(Option[SimpleRecord], Option[SimpleRecord])], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(ZipWhileEitherAvailable[SimpleRecord]())

      getMergedInputStreams ~>
        take(inNumberOfMessages) ~> toSimpleRecords ~> filterThreshold ~>
        filterDowntime ~> zip.in0
      outTopicHandler.topicSource ~> take(outNumberOfMessages) ~> toSimpleRecords ~> zip.in1

      SourceShape(zip.out)
    }
  }

  def getMergedInputStreams: Source[ConsumerRecord[String, String], NotUsed] = {
    Source.combine(inTopicHandler.head.topicSource, inTopicHandler(1).topicSource)(Merge(_))
  }

}
