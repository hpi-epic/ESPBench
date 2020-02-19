package org.hpi.esb.tpccdatagen.table

import java.util.Date

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.tpccdatagen.{CsvWriter, TpcCConstants, Utils}

object Workplace extends Logging with TpcCConstants {

  def create(outputDir: String, numberOfWarehouses: Int): Unit = {

    logger.info("Generating 'Workplace' records.")

    val workplaceRecord: Record = new Record(4)
    val csvWriter = new CsvWriter(s"$outputDir/workplace.csv")

    for (i <- 0 to numberOfWarehouses * 10) {
      var dateStart: Date = null
      var dateEnd: Date = null
      if (((Utils.getRandomNumber(1, 5) % 2) == 0 && i != 1) || i == 2) {
        dateStart = new java.sql.Date(System.currentTimeMillis() - 2000000000)
        dateEnd = new java.sql.Date(System.currentTimeMillis() + 2000000000)
      } else {
        dateStart = new java.sql.Date(System.currentTimeMillis())
        dateEnd = new java.sql.Date(System.currentTimeMillis())
      }
      workplaceRecord.reset()
      workplaceRecord.add(i)
      workplaceRecord.add(0) //TODO: make number of downtimes/workplace variable
      workplaceRecord.add(dateStart)
      workplaceRecord.add(dateEnd)
      csvWriter.addRecord(workplaceRecord)
    }
    csvWriter.close()
    logger.info("Workplace generation done.")
  }

}
