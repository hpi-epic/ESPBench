package org.hpi.esb.datavalidator.db

import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object DatabaseHandler {
  private var _connection = Database.forConfig("postgres")

  def connection: PostgresProfile.backend.Database = _connection

  def setConnection(conn: PostgresProfile.backend.Database): Unit = _connection = conn

  def terminate(): Unit = connection.close

  def executeStatement(statement: DBIO[Int]): Unit = {
    val future: Future[_] = {
      val importStatement: DBIO[Unit] = DBIO.seq(
        statement
      )
      connection.run(importStatement)
    }
    Await.result(future, Duration.Inf)
  }
}
