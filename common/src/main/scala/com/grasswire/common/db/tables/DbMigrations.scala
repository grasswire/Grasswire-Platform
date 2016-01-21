package com.grasswire.common.db.tables

import com.grasswire.common.DBDef
import com.grasswire.common.db.GWDatabaseDriver
import slick.lifted.ProvenShape
import scala.concurrent.ExecutionContext
import scalaz.Reader
import GWDatabaseDriver.api._
import com.grasswire.common.Implicits.TaskPimps

import scalaz.concurrent.Task

/**
 * Created by levinotik on 1/7/15.
 */

class DbMigrations(tag: Tag) extends Table[(String, Int)](tag, "DB_MIGRATIONS") {
  def status = column[String]("MIGRATION_STATUS")

  def version = column[Int]("MIGRATION_VERSION", O.Default(0))

  override def * : ProvenShape[(String, Int)] = (status, version)
}

object DbMigrations {
  def tableQuery = TableQuery[DbMigrations]

  def setMigrationStatus(status: String)(implicit ec: ExecutionContext): Reader[DBDef, Int] = Reader { db =>
    val q = for {
      m <- DbMigrations.tableQuery
    } yield m.status

    Task.fromScalaDeferred(db.run(q.result.headOption)).flatMap {
      case Some(_) =>
        Task.fromScalaDeferred(db.run(q.update(status)))
      case None =>
        Task.fromScalaDeferred(db.run(DbMigrations.tableQuery += ((status, 1))))
    }.run
  }
}
