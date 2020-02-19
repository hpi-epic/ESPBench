package org.hpi.esb.datasender.db.schema

import slick.lifted.TableQuery

object Schema {

  private val customer = TableQuery[Customer]
  private val history = TableQuery[History]
  private val order = TableQuery[Order]
  private val orderLine = TableQuery[OrderLine]
  private val item = TableQuery[Item]
  private val productionOrder = TableQuery[ProductionOrder]
  private val productionOrderLine = TableQuery[ProductionOrderLine]
  private val workplace = TableQuery[Workplace]

  val getForPostgres = List(
    customer,
    history,
    order,
    orderLine,
    item,
    productionOrder,
    productionOrderLine,
    workplace
  )
}
