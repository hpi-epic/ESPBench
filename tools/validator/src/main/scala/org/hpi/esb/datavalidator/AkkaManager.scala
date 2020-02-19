package org.hpi.esb.datavalidator

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datavalidator.db.DatabaseHandler

object AkkaManager extends Logging {

  val decider: Supervision.Decider = { e =>
    logger.error("Unhandled exception in stream: ", e)
    Supervision.Stop
  }

  lazy val system: ActorSystem = ActorSystem("HesseBenchValidator")
  val materializerSettings: ActorMaterializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  val materializer: ActorMaterializer = ActorMaterializer()(system)

  def terminate(): Unit = system.terminate()

  system.registerOnTermination({
    logger.info("Terminating all database connections.")
    DatabaseHandler.terminate()
  }
  )
}
