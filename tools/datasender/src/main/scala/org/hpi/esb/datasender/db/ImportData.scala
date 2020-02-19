package org.hpi.esb.datasender.db

import org.hpi.esb.commons.util.Logging

class ImportData extends Logging {

  def importErpData(): Unit = {

    new PostgresDB().importData()

  }

}