package com.grasswire.common.db

import com.grasswire.common.DBDef
import com.grasswire.common.db.tables._
import com.grasswire.common.logging.Logging
import com.typesafe.config.ConfigFactory
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import com.grasswire.common.db.GWDatabaseDriver.api._

/**
 * Levi Notik
 * Date: 2/10/14
 */
class DAL extends Logging {

  import DAL._

  val baseConfig = ConfigFactory.load().getConfig("grasswire-config")
  val dbConfig = baseConfig.getConfig("database-config")
  val dbConfigProps = dbConfig.getConfig("properties")
  val jdbcUrl = s"jdbc:postgresql://${dbConfig.getString("postgres-host")}:5432/${dbConfigProps.getString("databaseName")}"
  logger.info(s"connecting to database at $jdbcUrl")
  val dbUser = dbConfigProps.getString("user")
  val dbPassword = dbConfigProps.getString("password")
  val configString = s"""  database-config = {
                       |    url = "$jdbcUrl"
                       |    properties = {
                       |      databaseName = ${dbConfigProps.getString("databaseName")}
                       |      user = $dbUser
                       |      password = $dbPassword
                       |    }
                       |    numThreads = 20
                       |  }""".stripMargin

  val effectiveConfig = ConfigFactory.parseString(configString)

  val db: DBDef = Database.forConfig("database-config", effectiveConfig)

  def createMigrationTables: Future[Try[Unit]] = {
    val schemas = MigrationLocks.tableQuery.schema ++ DbMigrations.tableQuery.schema
    db.run(DBIO.seq(schemas.create).asTry)
  }

  def createTablesTask: Future[Seq[Unit]] = {

    val schemas = List(
      MigrationLocks.tableQuery,
      DbMigrations.tableQuery,
      SharedSecrets.tableQuery,
      Admins.tableQuery,
      Users.tableQuery,
      Digests.tableQuery,
      Links.tableQuery,
      Stories.tableQuery,
      Changelogs.tableQuery,
      IosOnboarding.tableQuery,
      SiteLockdowns.tableQuery,
      StoryChangelogs.tableQuery,
      LivePages.tableQuery,
      ContributorsStoryLinks.tableQuery)

    createIfNotExists(schemas: _*)(db)
  }
}

object DAL extends Logging {
  def createIfNotExists(tables: TableQuery[_ <: Table[_]]*)(db: DBDef): Future[Seq[Unit]] =
    Future.sequence(tables map { table =>
      db.run(tableExistsAction(table)) flatMap { exists =>
        if (exists) {
          Future.successful(())
        } else {
          logger.info(s"creating table ${table.baseTableRow.tableName}")
          db.run(DBIO.seq(table.schema.create))
        }
      }

    })

  def tableExistsAction(table: TableQuery[_ <: Table[_]]): dbio.DBIOAction[Boolean, NoStream, Read] = MTable.getTables(table.baseTableRow.tableName).headOption.map(_.isDefined)
}
