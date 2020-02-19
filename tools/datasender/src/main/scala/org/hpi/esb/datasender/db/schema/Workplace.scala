package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class Workplace(tag: Tag) extends Table[(Int, Int, Timestamp, Timestamp)] (tag, "workplace") {

  val wp_id = column[Int]("WP_ID")
  val wp_dt_id = column[Int]("WP_DT_ID")
  val wp_downtime_start = column[Timestamp]("WP_DOWNTIME_START")
  val wp_downtime_end = column[Timestamp]("WP_DOWNTIME_END")
  val * : ProvenShape[(Int, Int, Timestamp, Timestamp)] = (wp_id, wp_dt_id, wp_downtime_start, wp_downtime_end)
  val pk = primaryKey("pkWorkplace", (wp_id, wp_dt_id))

}
