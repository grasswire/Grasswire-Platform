package com.grasswire.api.app

import akka.actor.{ ActorRefFactory, ActorSystem, Props }
import akka.io.IO
import com.grasswire.api.config.APIConfig
import com.grasswire.common.CommonConfig
import com.grasswire.common.db._
import com.grasswire.common.email.Mailchimp
import com.grasswire.common.http.RoutedHttpService
import com.grasswire.common.logging.Logging
import com.grasswire.email.templates.EmailTemplates
import com.rabbitmq.client.Channel
import com.typesafe.config.ConfigFactory
import net.gpedro.integrations.slack.SlackApi
import org.joda.time.format.DateTimeFormat
import org.joda.time.{ DateTimeZone, DateTime }
import spray.can.Http
import com.grasswire.common.db.tables.ContributorsStoryLinks

import scalaz.concurrent.Task
import scalaz.{ -\/, \/, \/- }

trait Core {
  protected implicit def system: ActorSystem
  protected def redis: scredis.Redis
  CommonConfig.setGlobalLogger()
}

trait BootedCore extends Core with Api with Logging {

  implicit lazy val system: ActorSystem = ActorSystem("grasswire-api")
  import scala.concurrent.duration._

  def actorRefFactory: ActorRefFactory = system

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))
  val migrations = List(ContributorsStoryLinks.populateContributorsTableMigration)
  val migrationService = MigrationService(migrations, dal)
  def httpBindCommand = Http.Bind(rootService, "0.0.0.0", port = 8080)

  import com.grasswire.common.Implicits.TaskPimps

  def startMigration(): Unit =
    migrationService.run.runAsync(migrationHandler)

  def migrationHandler: Throwable \/ Unit => Unit = {

    case -\/(MigrationPending) =>
      logger.warn("Migration pending. Trying again in 10 seconds")
      Task.schedule(startMigration(), 10.seconds).runAsync(_ => ())

    case -\/(ex: Throwable) => shutDownWithFailure(ex, "MIGRATION FAILED, SHUTTING DOWN SERVICE!")

    case \/-(_) =>
      Task.fromScalaDeferred(dal.createTablesTask)
        .runAsync(_.fold(l => {
          shutDownWithFailure(l, "Failed to create table(s)")
        }, r => IO(Http)(system) ! httpBindCommand
        ))
  }

  Task.fromScalaDeferred(dal.createMigrationTables).attemptRun
  startMigration()
  sys.addShutdownHook(system.shutdown())

  private[this] def shutDownWithFailure(ex: Throwable, msg: String) = {
    logger.error(msg, ex)
    system.shutdown()
  }
}

trait CoreActors extends Logging { this: Core =>
  val baseConfig = ConfigFactory.load()
  val redis = scredis.Redis("application.conf", "scredis")
  val slackApi = new SlackApi(CommonConfig.slackHook)
  val dal: DAL = new DAL
  logger.info(CommonConfig.environment)

}
