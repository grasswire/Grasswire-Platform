package com.grasswire.common.db.tables

/**
 * Created by levinotik on 1/7/15.
 */

import com.grasswire.common.DBDef
import com.grasswire.common.db.GWDatabaseDriver
import GWDatabaseDriver.api._
import com.grasswire.common.Implicits.TaskPimps

import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext
import scalaz.Reader
import scalaz.concurrent.Task

class MigrationLocks(tag: Tag) extends Table[String](tag, "MIGRATION_LOCK") {
  def lock = column[String]("MIGRATION_LOCK", O.PrimaryKey)

  override def * : ProvenShape[String] = lock
}

object MigrationLocks {
  def tableQuery = TableQuery[MigrationLocks]
  val migrationLockKey = "DB_MIGRATION_LOCK_KEY"

  def getMigrationLock(implicit ec: ExecutionContext): Reader[DBDef, Int] = Reader(db =>
    Task.fromScalaDeferred(db.run(MigrationLocks.tableQuery += migrationLockKey)).run
  )

  def migrationLockRelease(implicit ec: ExecutionContext): Reader[DBDef, Int] = Reader { db =>
    val q = for {
      lock <- MigrationLocks.tableQuery
    } yield lock
    Task.fromScalaDeferred(db.run(q.delete)).run
  }
}
