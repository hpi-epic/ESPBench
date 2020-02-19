package org.hpi.esb.datavalidator.validation

import akka.NotUsed

import scala.concurrent.duration._
import akka.stream.{ActorMaterializer, DelayOverflowStrategy, Graph, SourceShape}
import akka.stream.scaladsl.{Flow, GraphDSL}
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.datavalidator.data.{Record, SimpleRecord}
import org.hpi.esb.datavalidator.kafka.TopicHandler
import org.hpi.esb.datavalidator.validation.graphstage.ZipWhileEitherAvailable
import com.github.gnni.outlierdetection.StochasticOutlierDetection

import scala.collection.immutable

object StochasticOutlierSelection {
  var perplexity = 30
  val iterations = 50

  val columnIdxValue1 = 2 //mf01 - Electrical Power Main Phase 1
  val columnIdxValue2 = 3 //mf02 - Electrical Power Main Phase 2

  var maxWindowCount = 500
  val outlierThreshold = 0.5
}

class StochasticOutlierSelectionValidation(inTopicHandler: List[TopicHandler],
                                           outTopicHandler: TopicHandler,
                                           materializer: ActorMaterializer)
  extends Validation[SimpleRecord](inTopicHandler, outTopicHandler, materializer) {

  override val valueName = "SimpleRecords"
  override val queryName: String = Configs.QueryNames.StochasticOutlierSelection

  override def getResponseTime(inRecord: Record, outRecord: Record): Long = {
    super.getResponseTime(inRecord, outRecord)
  }

  val createWindows: Flow[SimpleRecord, immutable.Seq[SimpleRecord], NotUsed] = {
    Flow[SimpleRecord].groupedWithin(StochasticOutlierSelection.maxWindowCount, 30 seconds).
      filter(seq => seq.toList.size == StochasticOutlierSelection.maxWindowCount)
  }

  val calculateOutliers: Flow[immutable.Seq[SimpleRecord], SimpleRecord, NotUsed] = {
    Flow[immutable.Seq[SimpleRecord]].mapConcat(sequence => {
      val windowEndTS = sequence.last.timestamp
      val inputArray = sequence.map(record => {
        val splitRecord = record.value.split("\t")
        Array[Double](splitRecord(StochasticOutlierSelection.columnIdxValue1).toDouble,
          splitRecord(StochasticOutlierSelection.columnIdxValue2).toDouble)
      }).toArray

      StochasticOutlierDetection.performOutlierDetection(inputArray,
        StochasticOutlierSelection.perplexity,
        StochasticOutlierSelection.iterations)
        .filter(_._2 >= StochasticOutlierSelection.outlierThreshold)
        .map(result => {
          val record = sequence(result._1.toInt)
          SimpleRecord(record.value + "\t" + "%.2f".format(result._2))(windowEndTS)
        }).toList.sortBy(_.value.split("\t")(1).toInt)
    })
  }

  def createSource(): Graph[SourceShape[(Option[SimpleRecord], Option[SimpleRecord])], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val zip = builder.add(ZipWhileEitherAvailable[SimpleRecord]())

      inTopicHandler.head.topicSource ~> take(inNumberOfMessages) ~> toSimpleRecords ~> createWindows ~> calculateOutliers ~> zip.in0
      outTopicHandler.topicSource ~> take(outNumberOfMessages) ~> toSimpleRecords ~> zip.in1

      SourceShape(zip.out)
    }
  }
}
