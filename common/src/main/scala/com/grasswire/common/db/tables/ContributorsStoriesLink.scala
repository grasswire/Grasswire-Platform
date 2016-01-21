package com.grasswire.common.db.tables

import com.grasswire.common.config.GWEnvironment
import com.grasswire.common.db.PostgresFunctions
import com.grasswire.common.json_models.LinkJsonModel._
import com.grasswire.common.json_models._
import com.grasswire.common.logging.Logging
import com.grasswire.common.models.{ JsonHelper, StoryEdits, Tweet }
import com.grasswire.common.parsers.{ PlainLinkType, TweetLinkType, VideoLinkType }
import com.grasswire.common.{ DBDef, LinkId, StoryId, Username }
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json.Json
import slick.dbio.Effect.Write
import slick.lifted.{ ProvenShape, Tag }
import slick.profile.FixedSqlAction
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scalaz._
import scalaz.concurrent.Task
import com.grasswire.common.db.GWDatabaseDriver.api._
import com.grasswire.common.db.DAL

case class ContributorStoryLinkPEntity(storyId: StoryId, contributor: Username)

class ContributorsStoryLinks(tag: Tag) extends Table[ContributorStoryLinkPEntity](tag, "contributors_story_links") {
  def storyId = column[Long]("story_id")
  def contributor = column[String]("contributor")
  def idx = index("idx_story_contributor", (storyId, contributor), unique = true)
  def * = (storyId, contributor) <> (ContributorStoryLinkPEntity.apply _ tupled, ContributorStoryLinkPEntity.unapply)
}

object ContributorsStoryLinks extends Logging {
  import com.grasswire.common.Implicits.TaskPimps
  import com.grasswire.common.db.migrations._

  val populateContributorsTableMigration = new GWMigration[Int] {
    def migrationType = ExtractTransformLoad
    def description = "This migration populates the ContributorsStoryLinks table with mappings of story <> contributor"
    def shouldRun(db: DBDef)(implicit ec: ExecutionContext): Task[Boolean] = {
      Task.fromScalaDeferred(db.run(DAL.tableExistsAction(ContributorsStoryLinks.tableQuery))).flatMap {
        case false =>
          logger.info(s"$description: table doesn't exist, migration WILL run")
          Task.now(true)
        case true =>
          val tableIsPopulatedQuery = ContributorsStoryLinks.tableQuery.length
          Task.fromScalaDeferred(db.run(tableIsPopulatedQuery.result)).map {
            case x =>
              val willRun = x <= 0
              if (willRun) {
                logger.info(s"$description: table exists but is not populated. Migration WILL run")
              } else {
                logger.info(s"$description: table exists but is populated. Migration WILL NOT run")

              }
              willRun
          }
      }
    }
    def migration(db: DBDef)(implicit ec: ExecutionContext): Task[Int] = {
      for {
        createTable <- Task.fromScalaDeferred(db.run(DAL.tableExistsAction(ContributorsStoryLinks.tableQuery))).flatMap {
          case false => Task.fromScalaDeferred(db.run(DBIO.seq(ContributorsStoryLinks.tableQuery.schema.create)))
          case true => Task.now(())
        }
        transform <- {
          val activeStoryIdsQ = Stories.tableQuery.map(_.id)
          val allLinks = Links.tableQuery.filter(_.storyId in activeStoryIdsQ)
            .map { link => (link.storyId, link.submittedById) }

          val contributionsQ =
            StoryChangelogs.tableQuery.filter(_.storyId in activeStoryIdsQ)
              .map { changelog => (changelog.storyId, changelog.updatedBy) } union allLinks

          Task.fromScalaDeferred(db.run(contributionsQ.result)).flatMap { xs =>
            val entities = xs.map { case (storyid, username) => ContributorStoryLinkPEntity(storyid, username) }
            ContributorsStoryLinks.insertMany(entities)(db)

          }
        }
      } yield transform
    }
  }

  def tableQuery = TableQuery[ContributorsStoryLinks]
  def insert(entity: ContributorStoryLinkPEntity)(db: DBDef)(implicit ec: ExecutionContext): Task[Int] = {
    val q = ContributorsStoryLinks.tableQuery += entity
    Task.fromScalaDeferred(db.run(q))
      .handle { case t: Throwable if t.getMessage.toLowerCase.contains("duplicate key value violates unique constraint") => 0 }
  }
  def insertMany(entities: Seq[ContributorStoryLinkPEntity])(db: DBDef)(implicit ec: ExecutionContext): Task[Int] = {
    val q = entities.map(e => ContributorsStoryLinks.tableQuery += e)
    Task.fromScalaDeferred(db.run(DBIO.sequence(q))).map(_.sum)
      .handle {
        case t: Throwable if t.getMessage.toLowerCase.contains("duplicate key value violates unique constraint") =>
          logger.info("ignoring duplicate key violation")
          0
      }
  }

}
