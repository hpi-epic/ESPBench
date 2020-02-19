package org.hpi.esb.datasender.db

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.datasender.db.schema.Schema
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PostgresDB extends Logging {

  val db: Database = Database.forConfig("postgres")
  val schema = Schema.getForPostgres

  def importData(): Unit = {
    try {
    truncateExistingTables()
    createTablesIfNotExistent()
    importDataFromCsv()
    } finally db.close
  }


  def truncateExistingTables(): Unit = {

    val existingTables = db.run(MTable.getTables)

    val truncateTableIfExistent = existingTables.flatMap( table => {
      val names = table.map(mt =>
        mt.name.name
      )
      val truncateTables = schema.filter( table =>
        (names.contains(table.baseTableRow.tableName)))
        .map(_.schema.truncate)
      db.run(DBIO.sequence(truncateTables))
    })
    Await.result(truncateTableIfExistent, Duration.Inf)
  }


  def createTablesIfNotExistent(): Unit = {

      val existingTables = db.run(MTable.getTables)

      val createTablesIfNotExistentExec = existingTables.flatMap( table => {
        val names = table.map(mt =>
          mt.name.name
        )

        val createIfNotExist = schema.filter( table =>
          (!names.contains(table.baseTableRow.tableName)))
          .map(_.schema.create)
        db.run(DBIO.sequence(createIfNotExist))
      })

      Await.result(createTablesIfNotExistentExec, Duration.Inf)

  }

  def importDataFromCsv(): Unit = {
    schema.map{
      table =>
        executeImport(table.baseTableRow.tableName)
    }
  }

  def executeImport(tableName: String): Unit = {
    val future: Future[_] = {
      val importStatement: DBIO[Unit] = DBIO.seq (
        sqlu"""COPY "#${tableName}" FROM '/home/benchmarker/data/#${tableName}.csv' WITH (FORMAT csv, NULL '\N', delimiter E'\t')"""
      )
      db.run(importStatement)
    }
      Await.result(future, Duration.Inf)
  }

}
