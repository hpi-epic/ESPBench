package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class History(tag: Tag) extends Table[(Int, Timestamp, Double, String)](tag, "history") {

  val h_c_id = column[Int]("H_C_ID")
  val h_date = column[Timestamp]("H_DATE")
  val h_amount = column[Double]("H_AMOUNT", O.SqlType("DECIMAL(6,2)"))
  val h_data = column[String]("H_DATA", O.SqlType("VARCHAR(24)"))
  val * : ProvenShape[(Int, Timestamp, Double, String)] = (h_c_id, h_date, h_amount, h_data)

}