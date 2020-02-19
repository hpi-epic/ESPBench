package org.hpi.esb.tpccdatagen

import java.io.FileInputStream
import java.util.Properties

import org.hpi.esb.commons.util.Logging
import java.nio.file._

import org.hpi.esb.tpccdatagen.table.{Customer, Item, Order, Workplace}

class DataGenerator extends Logging {

  private val properties = new Properties
  private final val NUMBER_OF_WAREHOUSES_PROPERTY = "NUMBER_OF_WAREHOUSES"
  private final val OUTPUT_DIR_PROPERTY = "OUTPUT_DIR"
  private final val DEFAULT_PROPERTIES_FILE: String = "tpc-c.properties"
  private final val PROJECT_LOCATION: String = "tools/tpc-c_gen/"

  private var numberOfWarehouses: Int = 0
  private var outputDir: String = ""

  def generateData(configFileName: String = DEFAULT_PROPERTIES_FILE): Unit = {
    loadConfig(configFileName)
    runGeneration()
  }

  def runGeneration(): Unit = {

    logger.info("Starting TPC-C Data Generation.")
    logger.info(s"Configuration: ${properties.toString}")

    Item.create(outputDir)
    Workplace.create(outputDir, numberOfWarehouses)
    Customer.create(outputDir, numberOfWarehouses)
    Order.create(outputDir, numberOfWarehouses)
    logger.info("Finished TPC-C Data Generation.")
  }

  def loadConfig(configFileName: String): Unit = {
    try {
      val inputStream: FileInputStream =
        if (Files.exists(Paths.get(configFileName))) {
          new FileInputStream(configFileName)
        } else {
          new FileInputStream(PROJECT_LOCATION + configFileName)
        }
      properties.load(inputStream)
      numberOfWarehouses = properties.getProperty(NUMBER_OF_WAREHOUSES_PROPERTY).toInt
      if (numberOfWarehouses < 1) {
        throw new RuntimeException(s"$NUMBER_OF_WAREHOUSES_PROPERTY has to be greater than 0.")
      }
      outputDir = PROJECT_LOCATION + properties.getProperty(OUTPUT_DIR_PROPERTY)
      createDirIfNotExistentOrDeleteOldFiles(outputDir)
    } catch {
      case e: Throwable =>
        logger.error(s"Exception when loading TPC-C data generator config: ${e.printStackTrace()}")
    }
  }

  private def createDirIfNotExistentOrDeleteOldFiles(directoryPath: String): Unit = {
    val path: Path = Paths.get(directoryPath)
    if (!Files.exists(path)) {
      Files.createDirectories(path)
    } else {
      Files.walk(path).toArray.drop(1).foreach(filePath =>
        {
          logger.info(s"Deleting old file in output dir: $filePath")
          Files.deleteIfExists(filePath.asInstanceOf[Path])
        }
      )
    }
  }

}