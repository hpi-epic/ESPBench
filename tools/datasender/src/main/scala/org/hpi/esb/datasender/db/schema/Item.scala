package org.hpi.esb.datasender.db.schema

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

class Item(tag: Tag) extends Table[(Int, Int, String, Double, String)] (tag, "item") {

  val i_id = column[Int]("I_ID", O.PrimaryKey)
  val i_im_id = column[Int]("I_IM_ID")
  val i_name = column[String]("I_NAME", O.SqlType("VARCHAR(24)"))
  val i_price = column[Double]("I_PRICE", O.SqlType("DECIMAL(5,2)"))
  val i_data = column[String]("I_DATA", O.SqlType("VARCHAR(50)"))
  val * : ProvenShape[(Int, Int, String, Double, String)] = (i_id, i_im_id, i_name, i_price, i_data)

}