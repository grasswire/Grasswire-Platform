package com.grasswire.common.db

import com.grasswire.common.db.migrations.GWMigration
import com.grasswire.common.db.tables.{ DbMigrations, MigrationLocks }
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scalaz.{ \/, -\/, \/- }
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.std.list._
import scalaz.std.option._
import scalaz.syntax.traverse._
import scalaz.concurrent.Task

case object MigrationPending extends Exception("Could not obtain migration lock")
case class MigrationFailed(t: Throwable) extends Exception(t)

case class MigrationService(migrations: List[GWMigration[_]], dal: DAL) {
  def run: Task[Unit] = {
    \/.fromTryCatchNonFatal(MigrationLocks.getMigrationLock.run(dal.db)) match {
      case -\/(e) => Task.fail(MigrationPending)
      case \/-(_) =>
        val sequence: Task[List[Unit]] = migrations.map(_.run(dal.db)).sequenceU
        sequence.map { _ =>
          MigrationLocks.migrationLockRelease.run(dal.db)
          DbMigrations.setMigrationStatus("success").run(dal.db)
          ()
        }.handleWith {
          case t: Throwable =>
            Task.fail {
              DbMigrations.setMigrationStatus("failure").run(dal.db);
              MigrationFailed(t)
            }
        }
    }
  }
}
