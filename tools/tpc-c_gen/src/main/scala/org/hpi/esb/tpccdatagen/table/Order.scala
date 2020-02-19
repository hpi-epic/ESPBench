package org.hpi.esb.tpccdatagen.table

import java.sql.Date

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.tpccdatagen.{CsvWriter, TpcCConstants, Utils}

object Order extends Logging with TpcCConstants {

  val customerIdPermutationArray: Array[Int] = Array.ofDim[Int](CUSTOMERS_PER_DISTRICT)
  var permutationIndex = 0

  def initCustomerIdPermutation(): Unit = {
    permutationIndex = 0
    val tempArray = Array.ofDim[Int](CUSTOMERS_PER_DISTRICT)
    for (i <- 0 until ORDERS_PER_DISTRICT) {
      customerIdPermutationArray(i) = i + 1
      tempArray(i) = i + 1
    }

    for (i <- 0 until (ORDERS_PER_DISTRICT - 1)) {
      val j: Int = Utils.getRandomNumber(i + 1, ORDERS_PER_DISTRICT - 1)
      customerIdPermutationArray(j) = tempArray(i)
    }
  }

  def getNextCustomerId: Int = {
    permutationIndex += 1
    customerIdPermutationArray(permutationIndex - 1)
  }

  def generateOrder(date: Date, newOrderId: Int, orderOrderLineCount: Int, csvWriter: CsvWriter, orderRecord: Record): Unit = {
    val orderCarrierId = Utils.getRandomNumber(1, 10)
    orderRecord.reset()
    orderRecord.add(newOrderId)
    orderRecord.add(getNextCustomerId)
    orderRecord.add(date)
    if (newOrderId > 2100) { //last 900 orders have not been delivered
      orderRecord.add(null)
    } else {
      orderRecord.add(orderCarrierId)
    }
    orderRecord.add(orderOrderLineCount)
    orderRecord.add(1)
    csvWriter.addRecord(orderRecord)
  }

  def generateOrderLine(date: Date, orderId: Int, newOrderId: Int, orderLineNumber: Int, orderLineItemId: Int,
                        warehouseId: Int, csvWriter: CsvWriter, orderLineRecord: Record): Unit = {
    val orderLineQuantity = 5
    val orderLineAmount = 0.0.toFloat
    val orderLineDistributionInfo = Utils.getRandomAlphaNumericString(24, 24)

    orderLineRecord.reset()
    orderLineRecord.add(newOrderId)
    orderLineRecord.add(orderLineNumber)
    orderLineRecord.add(orderLineItemId)
    orderLineRecord.add(warehouseId)

    if (orderId > 2100) {
      orderLineRecord.add(null)
      orderLineRecord.add(orderLineQuantity)
      orderLineRecord.add(orderLineAmount)
      orderLineRecord.add(orderLineDistributionInfo)
    } else {
      orderLineRecord.add(date)
      orderLineRecord.add(orderLineQuantity)
      orderLineRecord.add((Utils.getRandomNumber(10, 10000).toFloat / 100.0).toFloat)
      orderLineRecord.add(orderLineDistributionInfo)
    }
    csvWriter.addRecord(orderLineRecord)
  }

  def generateProductionOrder(date: Date, newOrderId: Int, orderLineNumber: Int, csvWriter: CsvWriter, productionOrderRecord: Record): Unit = {
    productionOrderRecord.reset()
    productionOrderRecord.add(newOrderId)
    productionOrderRecord.add(orderLineNumber)
    productionOrderRecord.add(date)
    csvWriter.addRecord(productionOrderRecord)
  }

  def generateProductionOrderLine(newOrderId: Int, orderLineNumber: Int, productionOrderLineNumber: Int, orderLineItemId: Int,
                                  numberOfWarehouses: Int, csvWriter: CsvWriter, productionOrderLineRecord: Record): Unit = {
    productionOrderLineRecord.reset()
    productionOrderLineRecord.add(newOrderId)
    productionOrderLineRecord.add(orderLineNumber)
    productionOrderLineRecord.add(productionOrderLineNumber)
    productionOrderLineRecord.add(orderLineItemId)
    productionOrderLineRecord.add(1)
    productionOrderLineRecord.add(Utils.getRandomNumber(0, numberOfWarehouses * 10))
    productionOrderLineRecord.add(null)
    productionOrderLineRecord.add(null)
    csvWriter.addRecord(productionOrderLineRecord)
  }

  def generateProductionTimes(prevOrderId: Int, newOrderId: Int, prevOrderLineNumber: Int, orderLineNumber: Int,
                              productionOrderLineNumber: Int, orderOrderLineCount: Int, productionTimesRecord: Record,
                              csvWriter: CsvWriter): Unit = {
    //TODO: how to delay end? atm, comes right after start
    if (prevOrderId >= 0) {
      productionTimesRecord.reset()
      productionTimesRecord.add(prevOrderId)
      productionTimesRecord.add(prevOrderLineNumber)
      productionTimesRecord.add(productionOrderLineNumber)
      productionTimesRecord.add(0)
      csvWriter.addRecord(productionTimesRecord)
    }
    productionTimesRecord.reset()
    productionTimesRecord.add(newOrderId)
    productionTimesRecord.add(orderLineNumber)
    productionTimesRecord.add(productionOrderLineNumber)
    productionTimesRecord.add(1)
    csvWriter.addRecord(productionTimesRecord)

    if (orderLineNumber == orderOrderLineCount) {
      productionTimesRecord.reset()
      productionTimesRecord.add(newOrderId)
      productionTimesRecord.add(orderLineNumber)
      productionTimesRecord.add(productionOrderLineNumber)
      productionTimesRecord.add(0)
      csvWriter.addRecord(productionTimesRecord)
    }
  }

  def create(outputDir: String, numberOfWarehouses: Int): Unit = {
    val orderRecord = new Record(6)
    val csvWriterOrder = new CsvWriter(s"$outputDir/order.csv")
    val productionOrderRecord = new Record(3)
    val csvWriterProductionOrder = new CsvWriter(s"$outputDir/production_order.csv")
    val productionOrderLineRecord = new Record(8)
    val csvWriterProductionOrderLine = new CsvWriter(s"$outputDir/production_order_line.csv")
    val orderLineRecord = new Record(8)
    val csvWriterOrderLine = new CsvWriter(s"$outputDir/order_line.csv")
    val productionTimesRecord = new Record(4)
    val csvWriterProductionTimes = new CsvWriter(s"$outputDir/production_times.csv")
    logger.info("Generating all kind of *order* records.")
    for (warehouseId <- 1 to numberOfWarehouses) {
      for (districtId <- 1 to DISTRICTS_PER_WAREHOUSE) {
        initCustomerIdPermutation()
        for (orderId <- 1 to ORDERS_PER_DISTRICT) {
          val date = new java.sql.Date(System.currentTimeMillis())
          val newOrderId = orderId + 10000 * districtId + 1000000 * warehouseId
          val orderOrderLineCount = Utils.getRandomNumber(5, 15)
          generateOrder(date, newOrderId, orderOrderLineCount, csvWriterOrder, orderRecord)
          var prevOrderId = -1
          var prevOrderLineNumber = -1

          for (orderLineNumber <- 1 to orderOrderLineCount) {
            val orderLineItemId = Utils.getRandomNumber(1, MAX_ITEMS)
            generateOrderLine(date, orderId, newOrderId, orderLineNumber, orderLineItemId,
              warehouseId, csvWriterOrderLine, orderLineRecord)
            generateProductionOrder(date, newOrderId, orderLineNumber, csvWriterProductionOrder, productionOrderRecord)
            val productionOrderLineNumber = 1
            generateProductionOrderLine(newOrderId, orderLineNumber, productionOrderLineNumber, orderLineItemId,
              numberOfWarehouses, csvWriterProductionOrderLine, productionOrderLineRecord)
            generateProductionTimes(prevOrderId, newOrderId, productionOrderLineNumber, orderLineNumber,
              productionOrderLineNumber, orderOrderLineCount, productionTimesRecord, csvWriterProductionTimes)
            prevOrderId = newOrderId
            prevOrderLineNumber = orderLineNumber
          }
        }
      }
    }
    csvWriterProductionOrder.close()
    csvWriterProductionOrderLine.close()
    csvWriterOrder.close()
    csvWriterOrderLine.close()
    csvWriterProductionTimes.close()
    logger.info("*Order* generation done.")
  }
}
