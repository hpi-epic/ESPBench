package org.hpi.esb.commons.output.writers

import java.io.File

import com.github.tototoshi.csv.CSVReader
import org.hpi.esb.commons.output.CSVOutput
import org.hpi.esb.commons.output.model.Result
import org.hpi.esb.commons.util.Logging

import scala.io.Source

abstract class ResultWriter(inputFilesPrefix: String, resultsPath: String,
                            outputFileName: String) extends Logging {

  def execute(): Unit = {
    val inputSources = getInputSources(resultsPath)
    val table = createResultTable(inputSources)
    writeResults(table, resultsPath, outputFileName)
  }

  def createResultTable(inputSources: List[Source]): List[List[String]] = {
    val intermediateResultMaps = getIntermediateResultMaps(inputSources)
    if (intermediateResultMaps.nonEmpty) {
      val finalResult = getFinalResult(intermediateResultMaps)
      finalResult.toTable()
    } else {
      logger.info("No benchmark series result files were found for merger.")
      sys.exit(1)
    }
  }

  def writeResults(mergedResults: List[List[String]], resultPath: String, fileName: String): Unit = {
    CSVOutput.write(mergedResults, resultPath, fileName)
  }

  def getIntermediateResultMaps(sources: List[Source]): List[Map[String, String]] = {
    val readers = sources.map(CSVReader.open)
    readers.flatMap(r => r.allWithHeaders())
  }

  def getInputSources(resultsPath: String): List[Source] = {
    val files = getListOfFiles(resultsPath)
    val filteredFiles = filterFilesByPrefix(files, inputFilesPrefix)
    filteredFiles.map(Source.fromFile)
  }

  def filterFilesByPrefix(files: List[File], prefix: String): List[File] = {
    files.filter(_.getName.startsWith(prefix))
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getFinalResult(runResultMaps: List[Map[String, String]]): Result
}
