package org.hpi.esb.datasender.db.schema

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class Customer(tag: Tag) extends Table[(Int, String, String, String, String, String, String,
  String, String, String, Timestamp, String, Double, Double, Double, Double, Int, Int, String)](tag, "customer") {

  val c_id = column[Int]("C_ID")
  val c_first = column[String]("C_FIRST", O.SqlType("VARCHAR(16)"))
  val c_middle = column[String]("C_MIDDLE", O.SqlType("CHAR(2)"))
  val c_last = column[String]("C_LAST", O.SqlType("VARCHAR(16)"))
  val c_street_1 = column[String]("C_STREET_1", O.SqlType("VARCHAR(20)"))
  val c_street_2 = column[String]("C_STREET_2", O.SqlType("VARCHAR(20)"))
  val c_city = column[String]("C_CITY", O.SqlType("VARCHAR(20)"))
  val c_state = column[String]("C_STATE", O.SqlType("CHAR(2)"))
  val c_zip = column[String]("C_ZIP", O.SqlType("CHAR(9)"))
  val c_phone = column[String]("C_PHONE", O.SqlType("VARCHAR(16)"))
  val c_since = column[Timestamp]("C_SINCE")
  val c_credit = column[String]("C_CREDIT", O.SqlType("CHAR(2)"))
  val c_credit_lim = column[Double]("C_CREDIT_LIM", O.SqlType("DECIMAL(12,2)"))
  val c_discount = column[Double]("C_DISCOUNT", O.SqlType("DECIMAL(4,4)"))
  val c_balance = column[Double]("C_BALANCE", O.SqlType("DECIMAL(12,2)"))
  val c_ytd_payment = column[Double]("C_YTD_PAYMENT", O.SqlType("DECIMAL(12,2)"))
  val c_payment_cnt = column[Int]("C_PAYMENT_CNT")
  val c_delivery_cnt = column[Int]("C_DELIVERY_CNT")
  val c_data = column[String]("C_DATA", O.SqlType("VARCHAR(500)"))
  val * : ProvenShape[(Int, String, String, String, String, String, String, String, String, String,
    Timestamp, String, Double, Double, Double, Double, Int, Int, String)] =
    (c_id, c_first, c_middle, c_last, c_street_1, c_street_2, c_city,
      c_state, c_zip, c_phone, c_since, c_credit, c_credit_lim, c_discount, c_balance, c_ytd_payment,
      c_payment_cnt, c_delivery_cnt, c_data)
  val pk = primaryKey("pkCustomer", (c_id))

}
