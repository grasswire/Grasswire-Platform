package com.grasswire.common.db

object PostgresFunctions {

  import com.grasswire.common.db.GWDatabaseDriver.api._

  def rankOver[T0, T1](partition: Rep[T0], ordering: Rep[T1], descending: Boolean = false): Rep[Int] = {
    val expr = SimpleExpression.binary[T0, T1, Int] { (partitionExpr, orderByExpr, builder) =>
      // see http://www.postgresql.org/docs/current/static/tutorial-window.html
      builder.sqlBuilder += "(rank() over (partition by "
      builder.expr(partitionExpr)
      builder.sqlBuilder += " order by "
      builder.expr(orderByExpr)
      if (descending) builder.sqlBuilder += " desc"
      builder.sqlBuilder += "))"
      ()
    }
    expr(partition, ordering)
  }
}
