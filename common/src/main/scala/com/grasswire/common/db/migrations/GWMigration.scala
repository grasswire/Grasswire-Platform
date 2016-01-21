package com.grasswire.common.db.migrations

import com.grasswire.common.DBDef
import com.grasswire.common.db.{ GWDatabaseDriver, DAL }
import org.slf4j.LoggerFactory
import slick.dbio
import slick.dbio.Effect.Read

import slick.jdbc.meta.MTable
import slick.lifted.TableQuery
import scala.concurrent.{ ExecutionContext, Future }
import GWDatabaseDriver.api._
import com.grasswire.common.logging.Logging
import com.grasswire.common.Implicits.TaskPimps
import scalaz.concurrent.Task

sealed trait MigrationType
case object ExtractTransformLoad extends MigrationType

trait GWMigration[Result] extends Logging {
  def shouldRun(db: DBDef)(implicit ec: ExecutionContext): Task[Boolean]
  def migration(db: DBDef)(implicit ec: ExecutionContext): Task[Result]

  def migrationType: MigrationType

  def run(db: DBDef)(implicit ec: ExecutionContext): Task[Unit] = {
    shouldRun(db).flatMap {
      case true =>
        migrationType match {
          case ExtractTransformLoad =>
            migration(db).map(_ => logger.info(s"Completed migration $description"))
        }
      case false => Task.now(logger.info(s"Skipping unrequired migration $description"))
    }
  }

  def description: String
}
