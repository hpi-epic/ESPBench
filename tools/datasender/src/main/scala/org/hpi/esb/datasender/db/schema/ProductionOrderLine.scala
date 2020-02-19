package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class ProductionOrderLine(tag: Tag) extends Table[(Int, Int, Int, Int, Int, Int, Option[Timestamp], Option[Timestamp])](tag, "production_order_line") {

  val pol_o_id = column[Int]("POL_O_ID")
  val pol_ol_number = column[Int]("POL_OL_NUMBER")
  val pol_number = column[Int]("POL_NUMBER")
  val pol_i_id = column[Int]("POL_I_ID")
  val pol_quantity = column[Int]("POL_QUANTITY")
  val pol_wp_id = column[Int]("POL_WP_ID")
  val pol_start_ts = column[Option[Timestamp]]("POL_START_TS")
  val pol_end_ts = column[Option[Timestamp]]("POL_END_TS")
  val * : ProvenShape[(Int, Int, Int, Int, Int, Int, Option[Timestamp], Option[Timestamp])] =
    (pol_o_id, pol_ol_number, pol_number, pol_i_id, pol_quantity, pol_wp_id, pol_start_ts, pol_end_ts)
  val pk = primaryKey("pkProductionOrderLine", (pol_o_id, pol_ol_number, pol_number))
}
