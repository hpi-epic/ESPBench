package org.hpi.esb.datavalidator.validation.graphstage

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.collection.mutable


final class IgnoreLastElements[E](ignoreCount: Int)
  extends GraphStage[FlowShape[E, E]] {

  val in = Inlet[E]("IgnoreLastElement.in")
  val out = Outlet[E]("IgnoreLastElement.out")

  override def shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {

    var isBuffered = false
    val buffer: mutable.Queue[E] = new mutable.Queue[E]

    setHandlers(in, out, new InHandler with OutHandler {

      override def onPush(): Unit = {

        buffer.enqueue(grab(in))
        if(buffer.size == ignoreCount + 1) {
          push(out, buffer.dequeue())
        } else {
          // As long as the buffer is not full, nothing will be sent downstream.
          // As a result the downstream component will not call 'onPull' and we have
          // to manually pull upstream
          pull(in)
        }
      }

      override def onPull(): Unit = {
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        completeStage()
      }
    })
  }
}
