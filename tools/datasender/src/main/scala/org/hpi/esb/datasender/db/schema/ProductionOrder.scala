package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class ProductionOrder(tag: Tag) extends Table[(Int, Int, Timestamp)](tag, "production_order") {

  val po_o_id = column[Int]("PO_O_ID")
  val po_ol_number = column[Int]("PO_OL_NUMBER")
  val po_date = column[Timestamp]("PO_DATE")
  val * : ProvenShape[(Int, Int, Timestamp)] = (po_o_id, po_ol_number, po_date)
  val pk = primaryKey("pkProductionOrder", (po_o_id, po_ol_number))
}
