package org.hpi.esb.datavalidator.validation

import java.sql.Timestamp
import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL}
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.AkkaManager
import org.hpi.esb.datavalidator.data.SimpleRecord
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.validation.graphstage.ZipWhileEitherAvailable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Await}
import scala.concurrent.duration._

object ProcessingTimesValidation {
  val session = Database.forConfig("postgres")
  AkkaManager.system.registerOnTermination(session.close())
}

class ProcessingTimesValidation(inTopicHandler: List[TopicHandler],
                                outTopicHandler: TopicHandler,
                                materializer: ActorMaterializer)
  extends Validation[SimpleRecord](inTopicHandler, outTopicHandler, materializer) {

  override val valueName: String = "ProcessingTimes Records"
  override val queryName: String = Configs.QueryNames.ProcessingTimesQuery

  override def createSource(): Graph[SourceShape[(Option[SimpleRecord], Option[SimpleRecord])], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(ZipWhileEitherAvailable[SimpleRecord]())

      inTopicHandler.head.topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> zip.in0
      inTopicHandler.head.topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> checkDatabase ~> zip.in1

      SourceShape(zip.out)
    }
  }

  def checkDatabase: Flow[SimpleRecord, SimpleRecord, NotUsed] = {
    Flow[SimpleRecord].map(record => {
      val recordAsList = record.value.split("\t")
      val pol_o_id = recordAsList.head.toInt
      val pol_ol_number = recordAsList(1).toInt
      val pol_number = recordAsList(2).toInt
      val startOrEnd = recordAsList.last.toInt
      try {
        val query =
          sql"""SELECT "POL_O_ID", "POL_OL_NUMBER", "POL_NUMBER", "POL_START_TS", "POL_END_TS" FROM production_order_line WHERE "POL_O_ID" = $pol_o_id AND "POL_OL_NUMBER" = $pol_ol_number AND "POL_NUMBER" = $pol_number""".as[(Int, Int, Int, Timestamp, Timestamp)]
        val resVector = Await.result(MachinePowerValidation.session.run(query), 20.seconds)
        if (resVector.nonEmpty) {
          val res = resVector.head
          val idsDelemitedByTab = s"${res._1}\t${res._2}\t${res._3}\t$startOrEnd"
          if (startOrEnd == 1) {
            if (res._4 == null) {
              null
            } else {
              new SimpleRecord(value = idsDelemitedByTab)(res._4.getTime)
            }
          } else {
            if (res._5 == null) {
              null
            } else {
              new SimpleRecord(value = idsDelemitedByTab)(res._5.getTime)
            }
          }
        } else {
          null
        }
      } catch {
        case e: Throwable =>
          logger.error(s"DB exception in $queryName validation: ${e.printStackTrace()}")
          null
      }
    }
    )
  }

}
