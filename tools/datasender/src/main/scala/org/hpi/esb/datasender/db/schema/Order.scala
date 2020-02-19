package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class Order(tag: Tag) extends Table[(Int, Int, Timestamp, Option[Int], Int, Int)](tag, "order") {

  val o_id = column[Int]("O_ID", O.PrimaryKey)
  val o_c_id = column[Int]("O_C_ID")
  val o_entry_d = column[Timestamp]("O_ENTRY_D")
  val o_carrier_id = column[Option[Int]]("O_CARRIER_ID")
  val o_ol_cnt = column[Int]("O_OL_CNT")
  val o_all_local = column[Int]("O_ALL_LOCAL")
  val * : ProvenShape[(Int, Int, Timestamp, Option[Int], Int, Int)] = (o_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local)

}