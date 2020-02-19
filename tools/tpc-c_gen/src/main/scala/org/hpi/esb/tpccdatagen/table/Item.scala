package org.hpi.esb.tpccdatagen.table

import org.hpi.esb.commons.util.Logging
import org.hpi.esb.tpccdatagen.{CsvWriter, TpcCConstants, Utils}

object Item extends Logging with TpcCConstants {

  def create(outputDir: String): Unit = {

    val itemArray: Array[Int] = Array.ofDim[Int](MAX_ITEMS + 1)
    var pos = 0

    logger.info("Generating 'Item' records.")

    for (i <- 0 until (MAX_ITEMS / 10)) {
      itemArray(i) = 0
    }
    for (i <- 0 until (MAX_ITEMS / 10)) {
      do {
        pos = Utils.getRandomNumber(0, MAX_ITEMS)
      } while (itemArray(pos) != 0)
      itemArray(pos) = 1
    }
    val itemRecord: Record = new Record(5)
    val csvWriter = new CsvWriter(s"$outputDir/item.csv")

    for (itemId <- 1 to MAX_ITEMS) {
      val itemImageId = Utils.getRandomNumber(1, 10000)
      val itemName = Utils.getRandomAlphaNumericString(14, 24)

      val itemPrice = (Utils.getRandomNumber(100, 10000) / 100.0).toFloat
      var itemData = Utils.getRandomAlphaNumericString(26, 50)
      if (itemArray(itemId) != 0) {
        pos = Utils.getRandomNumber(0, itemData.length() - 8)
        itemData = itemData.substring(0, pos) + "original" + itemData.substring(pos + 8)
      }

      itemRecord.reset()
      itemRecord.add(itemId) //i_id
      itemRecord.add(itemImageId) //i_im_id
      itemRecord.add(itemName) //i_name
      itemRecord.add(itemPrice) //i_price
      itemRecord.add(itemData) //i_data

      csvWriter.addRecord(itemRecord)
    }

    csvWriter.close()
    logger.info("Item generation done.")
  }

}
