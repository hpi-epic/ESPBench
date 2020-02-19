package org.hpi.esb.tpccdatagen.table

import java.util.{Calendar, Date}

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.tpccdatagen.{CsvWriter, TpcCConstants, Utils}

object Customer extends Logging with TpcCConstants {

  def create(outputDir: String, numberOfWarehouses: Int): Unit = {
    logger.info("Generating 'Customer' and 'History' records.")
    val customerRecord: Record = new Record(19)
    val csvWriterCustomer = new CsvWriter(s"$outputDir/customer.csv")
    val historyRecord = new Record(4)
    val csvWriterHistory = new CsvWriter(s"$outputDir/history.csv")

    for (warehouseId <- 1 to numberOfWarehouses) {
      for (districId <- 1 to DISTRICTS_PER_WAREHOUSE) {
        for (customerId <- 1 to CUSTOMERS_PER_DISTRICT) {
          val customerFirst = Utils.getRandomAlphaNumericString(8, 16)
          val customerMiddle = "O" + "E"
          val customerLast = if (customerId <= 1000) {
            getLastName(customerId - 1)
          } else {
            getLastName(Utils.getNURand(255, 0, 999))
          }

          val customerStreet1 = Utils.getRandomAlphaNumericString(10, 20)
          val customerStreet2 = Utils.getRandomAlphaNumericString(10, 20)
          val customerCity = Utils.getRandomAlphaNumericString(10, 20)
          val customerState = Utils.getRandomAlphaNumericString(2, 2)
          val customerZip = Utils.getRandomAlphaNumericString(9, 9)
          val customerPhone = Utils.getRandomNumberString(16, 16)
          var customerCredit = if (Utils.getRandomNumber(0, 1) == 1) {
            "G"
          } else {
            "B"
          }
          customerCredit += "C"

          val customerCreditLimit = 50000
          val customerDiscount = ((Utils.getRandomNumber(0, 50)).toFloat / 100.0).toFloat
          val customerBalance = -10.0.toFloat

          val customerData = Utils.getRandomAlphaNumericString(300, 500)
          val calendar = Calendar.getInstance()
          val date = new java.sql.Date(calendar.getTimeInMillis)
          val customerIdUnique = customerId + 10000 * districId + 1000000 * warehouseId
          customerRecord.reset()
          customerRecord.add(customerIdUnique)
          customerRecord.add(customerFirst)
          customerRecord.add(customerMiddle)
          customerRecord.add(customerLast)
          customerRecord.add(customerStreet1)
          customerRecord.add(customerStreet2)
          customerRecord.add(customerCity)
          customerRecord.add(customerState)
          customerRecord.add(customerZip)
          customerRecord.add(customerPhone)
          customerRecord.add(date)
          customerRecord.add(customerCredit)
          customerRecord.add(customerCreditLimit)
          customerRecord.add(customerDiscount)
          customerRecord.add(customerBalance)
          customerRecord.add(10.0)
          customerRecord.add(1)
          customerRecord.add(0)
          customerRecord.add(customerData)

          csvWriterCustomer.addRecord(customerRecord)

          val historyAmount = 10.0
          val historyData = Utils.getRandomAlphaNumericString(12, 24)

          historyRecord.reset()
          historyRecord.add(customerIdUnique)
          historyRecord.add(date)
          historyRecord.add(historyAmount)
          historyRecord.add(historyData)
          csvWriterHistory.addRecord(historyRecord)
        }
      }
    }
    csvWriterCustomer.close()
    csvWriterHistory.close()
    logger.info("Customer and History generation done.")
  }

  def getLastName(id: Int): String = {
    val nameArray =
      Array("BAR", "OUGHT", "ABLE", "PRI", "PRES",
        "ESE", "ANTI", "CALLY", "ATION", "EING")

    var name = nameArray(id / 100)
    name = name + nameArray((id / 10) % 10)
    name + nameArray(id % 10)
  }
}
