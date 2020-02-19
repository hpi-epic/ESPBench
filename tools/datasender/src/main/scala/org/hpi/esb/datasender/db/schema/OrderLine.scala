package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class OrderLine(tag: Tag) extends Table[(Int, Int, Int, Int, Option[Timestamp], Int, Double, String)](tag, "order_line") {

  val ol_o_id = column[Int]("OL_O_ID")
  val ol_number = column[Int]("OL_NUMBER")
  val ol_i_id = column[Int]("OL_I_ID")
  val ol_supply_w_id = column[Int]("OL_SUPPLY_W_ID")
  val ol_delivery_d = column[Option[Timestamp]]("OL_DELIVERY_ID")
  val ol_quantity = column[Int]("OL_QUANTITY")
  val ol_amount = column[Double]("OL_AMOUNT", O.SqlType("DECIMAL(6,2)"))
  val ol_dist_info = column[String]("OL_DIST_INFO", O.SqlType("CHAR(24)"))
  val * : ProvenShape[(Int, Int, Int, Int, Option[Timestamp], Int, Double, String)] =
    (ol_o_id, ol_number, ol_i_id, ol_supply_w_id, ol_delivery_d, ol_quantity, ol_amount, ol_dist_info)
  val pk = primaryKey("pkOrderLine", (ol_o_id, ol_number))

}