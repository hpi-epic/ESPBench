package org.hpi.esb.datavalidator.validation.graphstage

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FanInShape2, Inlet, Outlet}

object ZipWhileEitherAvailable {
  def apply[T](): ZipWhileEitherAvailable[T, T, (Option[T], Option[T])] = {
    new ZipWhileEitherAvailable(Tuple2.apply[Option[T], Option[T]])
  }
}

class ZipWhileEitherAvailable[A1, A2, O](val zipper: (Option[A1], Option[A2]) => O) extends GraphStage[FanInShape2[A1, A2, O]] {

  private val name = "CustomZip"
  override val shape: FanInShape2[A1, A2, O] = new FanInShape2[A1, A2, O](name)
  val in0: Inlet[A1] = shape.in0
  val in1: Inlet[A2] = shape.in1

  override def initialAttributes = Attributes.name(name)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    // 'pending' is a status about who has pulled and pushed
    // if 'pending' > 0 upstream has not pushed and downstream has pulled
    // if 'pending' < 0 upstream has pushed and downstream has not pulled
    // if 'pending' == 0 upstream has pushed and downstream has pulled --> we can push
    var in0Pending = 0
    var in1Pending = 0

    var in0Finished = false
    var in1Finished = false

    private def pushAll(): Unit = {
      val elem1: Option[A1] = if (isAvailable(in0)) Some(grab(in0)) else None
      val elem2: Option[A2] = if (isAvailable(in1)) Some(grab(in1)) else None

      push(out, zipper(elem1, elem2))
      if (in0Finished && in1Finished) {
        completeStage()
      }
      else {
        if (!in0Finished) {
          pull(in0)
        }
        if (!in1Finished) {
          pull(in1)
        }
      }
    }

    override def preStart(): Unit = {
      pull(in0)
      pull(in1)
    }

    // define how to handle a push from 'in0'
    setHandler(in0, new InHandler {
      override def onPush(): Unit = {
        in0Pending -= 1
        if (in0Pending == 0 && in1Pending == 0) pushAll()
      }

      override def onUpstreamFinish(): Unit = {
        in0Finished = true
        if (in1Finished) {
          completeStage()
        } else if (in0Pending == 1) {
          in0Pending -= 1
          if (in1Pending == 0) {
            // in1 has an element waiting to be pushed
            pushAll()
          }

        }
      }
    })

    // define how to handle a push from 'in1'
    setHandler(in1, new InHandler {
      override def onPush(): Unit = {
        in1Pending -= 1
        if (in0Pending == 0 && in1Pending == 0) pushAll()
      }

      override def onUpstreamFinish(): Unit = {
        in1Finished = true
        if (in0Finished) {
          completeStage()

        } else if (in1Pending == 1) {
          in1Pending -= 1
          if (in0Pending == 0) {
            // in0 has an element waiting to be pushed
            pushAll()
          }
        }
      }

    })

    // define how to handle a pull from 'out'
    setHandler(out, new OutHandler {
      override def onPull(): Unit = {

        if(!in0Finished) {
          in0Pending += 1
        }
        if(!in1Finished) {
          in1Pending += 1
        }
        if (in0Pending == 0 && in1Pending == 0) pushAll()
      }
    })
  }

  def out: Outlet[O] = shape.out

  override def toString = name

}

