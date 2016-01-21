package com.grasswire.common.db.tables

import com.grasswire.common.json_models._
import com.grasswire.common.models.{ StoryEdits, Paging, PagedResult }
import com.grasswire.common.{ DBDef, Username }
import com.grasswire.common.db.GWDatabaseDriver
import GWDatabaseDriver.api._
import org.joda.time.{ DateTimeZone, DateTime }
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scalaz.concurrent.Task
import scalaz.{ ReaderT, Kleisli }
import com.grasswire.common.Implicits.TaskPimps
import scala.concurrent.ExecutionContext.Implicits.global
import com.grasswire.common.config.GWEnvironment

case class ChangelogPersistEntity(user: Username, action: String, storyId: Long, linkId: Option[Long], reason: Option[String], datetime: Long = DateTime.now(DateTimeZone.UTC).getMillis, id: Option[Long] = None)

class Changelogs(tag: Tag) extends Table[ChangelogPersistEntity](tag, "change_logs") {

  def user = column[String]("user")
  def action = column[String]("action")
  def storyId = column[Long]("story_id")
  def linkId = column[Long]("link_id")
  def reason = column[String]("reason")
  def datetime = column[Long]("datetime")
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def * = (user, action, storyId, linkId.?, reason.?, datetime, id.?) <> (ChangelogPersistEntity.apply _ tupled, ChangelogPersistEntity.unapply)

}

object Changelogs {

  implicit class ChangeLogOps(entity: ChangelogPersistEntity) {
    def toJson = ChangelogJsonModel(entity.user, entity.action, entity.storyId, entity.linkId, entity.reason, entity.datetime, entity.id.get)
  }

  import com.grasswire.common.Implicits.TaskPimps
  def tableQuery = TableQuery[Changelogs]

  def insert(editLog: ChangelogPersistEntity)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, Int] = Kleisli[Task, DBDef, Int] { db =>
    val q = Changelogs.tableQuery += editLog
    Task.fromScalaDeferred(db.run(q))
  }

  def list(offset: Int, max: Int)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, PagedResult[ChangelogJsonModel]] = Kleisli[Task, DBDef, PagedResult[ChangelogJsonModel]] { db =>

    val qTotal = Changelogs.tableQuery.length
    val q = Changelogs.tableQuery.sortBy(_.id.desc).drop(offset).take(max)

    for {
      total <- Task.fromScalaDeferred(db.run(qTotal.result))
      changes <- Task.fromScalaDeferred(db.run(q.result)).map(_.map(_.toJson))
    } yield PagedResult(changes.toList, Paging(offset, max, total))

  }
}

case class StoryChangelogPEntity(name: String,
  updatedBy: Username,
  summary: Option[String],
  headline: Option[String],
  coverPhoto: Option[String],
  hidden: Boolean,
  storyId: Long,
  id: Option[Long] = None)

class StoryChangelogs(tag: Tag) extends Table[StoryChangelogPEntity](tag, "story_change_logs") {

  def name = column[String]("story_name")

  def updatedBy = column[String]("updated_by")

  def summary = column[Option[String]]("summary", O.SqlType("TEXT"))

  def headline = column[Option[String]]("headline")

  def coverPhoto = column[Option[String]]("cover_photo")

  def hidden = column[Boolean]("hidden", O.Default(false))

  def storyId = column[Long]("story_id")

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def * = (name, updatedBy, summary, headline, coverPhoto, hidden, storyId, id.?) <> (StoryChangelogPEntity.apply _ tupled, StoryChangelogPEntity.unapply)
}

object StoryChangelogs {

  implicit class ChangeLogOps(entity: StoryChangelogPEntity) {
    def toJson = StoryChangelogJsonModel(entity.name, entity.updatedBy, entity.summary, entity.headline, entity.coverPhoto, entity.hidden, entity.storyId, entity.id.get)
  }

  def tableQuery = TableQuery[StoryChangelogs]
  def insertStoryChange(story: StoryPEntity)(db: DBDef): Task[Int] = {
    val q = StoryChangelogs.tableQuery += StoryChangelogPEntity(name = story.name, updatedBy = story.updatedBy, summary = story.summary, coverPhoto = story.coverPhoto, headline = story.headline, hidden = story.hidden, storyId = story.id.get)
    Task.fromScalaDeferred(db.run(q))
  }

  def revert(changelogId: Long, storyId: Long, reversion: StoryReversionJsonModel, username: String)(env: GWEnvironment): Task[StoryEdits] = {

    val log = StoryChangelogs.tableQuery.filter(cl => cl.storyId === storyId && cl.id === changelogId)
    Task.fromScalaDeferred(env.db.run(log.result.headOption)).flatMap {
      case Some(cl) =>
        Stories.update(storyId,
          username,
          if (reversion.revertName) Some(cl.name) else None,
          if (reversion.revertHeadline) cl.headline else None,
          if (reversion.revertSummary) cl.summary else None,
          if (reversion.revertCoverPhoto) cl.coverPhoto else None,
          if (reversion.revertHidden) Some(cl.hidden) else None

        ).run(env)
      case _ => Task(StoryEdits(Nil))
    }
  }

  def list(storyId: Long, offset: Int, max: Int)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, PagedResult[StoryChangelogJsonModel]] = Kleisli[Task, DBDef, PagedResult[StoryChangelogJsonModel]] { db =>
    val q = StoryChangelogs.tableQuery.filter(_.storyId === storyId).sortBy(_.id.desc).drop(offset).take(max)
    val qTotal = StoryChangelogs.tableQuery.filter(_.storyId === storyId).length
    for {
      total <- Task.fromScalaDeferred(db.run(qTotal.result))
      changes <- Task.fromScalaDeferred(db.run(q.result)).map(_.map(_.toJson))
    } yield PagedResult(changes.toList, Paging(offset, max, total))

  }
}
