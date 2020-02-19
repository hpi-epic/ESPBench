package org.hpi.esb.tpccdatagen

object Main {
  def main(args: Array[String]): Unit = {

    if (args.isEmpty) {
      new DataGenerator().generateData()
    } else {
      new DataGenerator().generateData(args.head)
    }

  }
}