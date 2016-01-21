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
import com.grasswire.common.Datastore

/**
 * Created by levinotik on 6/22/15.
 */

import com.grasswire.common.db.GWDatabaseDriver.api._

case class LinkPEntity(storyId: Long,
  submittedBy: Username,
  createdAt: Long,
  updatedAt: Long,
  hidden: Boolean,
  jsonData: String,
  linkType: String,
  id: Option[Long] = None)

object LinkPEntity extends Logging {

  implicit class LinkPEntityOps(entity: LinkPEntity) {
    def toJson(db: DBDef)(implicit ec: ExecutionContext): Task[LinkJsonModel] =
      Task(Users.findUser(entity.submittedBy)(db).get)
        .flatMap(_.hydrate(db)).map(toJsonPure)

    // FIXME exceptionful thus not really pure, should be Try[LinkJsonModel] or Task[LinkJsonModel]
    def toJsonPure(submittedBy: UserJsonModel): LinkJsonModel =
      entity.linkType match {
        case "plain_link" =>
          PlainLinkJsonModel(
            Json.parse(entity.jsonData).validate[PlainLinkJsonData].get,
            entity.submittedBy,
            entity.storyId,
            entity.createdAt,
            entity.hidden,
            entity.id.get,
            submittedBy)
        case "tweet" =>
          TweetLinkJsonModel(
            Json.parse(entity.jsonData).validate[Tweet].get,
            entity.submittedBy,
            entity.storyId,
            entity.createdAt,
            entity.hidden,
            entity.id.get,
            submittedBy)
        case "video" =>
          VideoLinkJsonModel(
            Json.parse(entity.jsonData).validate[VideoLinkJsonData].get,
            entity.submittedBy,
            entity.storyId,
            entity.createdAt,
            entity.hidden,
            entity.id.get,
            submittedBy)
        case _ => throw new Exception("unknown link type")
      }
  }
}

class Links(tag: Tag) extends Table[LinkPEntity](tag, "links") {

  def storyId = column[Long]("story_id")

  def story = foreignKey("story_id_fk", storyId, Stories.tableQuery)(_.id,
    onUpdate = ForeignKeyAction.Restrict,
    onDelete = ForeignKeyAction.Cascade)

  def submittedById = column[String]("submitted_by_id")

  def createdAt = column[Long]("created_at")

  def updatedAt = column[Long]("updated_at")

  def hidden = column[Boolean]("hidden", O.Default(false))

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def jsonData = column[String]("json_data", O.SqlType("TEXT"))

  def linkType = column[String]("link_type")

  override def * : ProvenShape[LinkPEntity] =
    (
      storyId,
      submittedById,
      createdAt,
      updatedAt,
      hidden,
      jsonData,
      linkType,
      id.?
    ) <> (LinkPEntity.apply _ tupled, LinkPEntity.unapply)
}

object Links extends Logging {

  import com.grasswire.common.Implicits.TaskPimps

  def contributors(
    since: Long = DateTime.now(DateTimeZone.UTC).minusHours(24).getMillis)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, List[UserJsonModel]] =
    Kleisli[Task, DBDef, List[UserJsonModel]] { db =>
      val q = Links.tableQuery.filter(_.createdAt >= since)
        .map(_.submittedById).groupBy(x => x).map(_._1)
      val q2 = Stories.tableQuery.filter(_.updatedAt >= since)
        .map(_.updatedById).groupBy(x => x).map(_._1)
      val q3 = Changelogs.tableQuery.filter(_.datetime >= since)
        .map(_.user).groupBy(x => x).map(_._1)
      val todaysSubmitterIds = (q union q2 union q3).result
      Task.fromScalaDeferred(db.run(todaysSubmitterIds))
        .flatMap(usernames => Users.findMany(usernames.toList)(db))
    }

  import LinkPEntity._

  import scalaz.std.list._
  import scalaz.std.option._
  import scalaz.syntax.traverse._

  def tableQuery = TableQuery[Links]

  def update(
    linkId: LinkId,
    updatedBy: Username,
    storyId: Option[Long],
    newThumbnail: Option[String],
    newTitle: Option[String],
    newDescription: Option[String],
    hidden: Option[Boolean])(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[Int]] { env =>
    val db = env.db
    val now = com.grasswire.common.nowUtcMillis
    val link = Links.findById(linkId).run(db)
    val updates = List(storyId, newThumbnail, newTitle, newDescription, hidden).flatten

    if (updates.nonEmpty) {
      link.flatMap {
        case Some(l: PlainLinkJsonModel) =>
          val newLinkData = l.linkData.copy(
            thumbnail = newThumbnail.orElse(l.linkData.thumbnail),
            description = newDescription.getOrElse(l.linkData.description),
            title = newTitle.getOrElse(l.linkData.title))
          val action = Links.tableQuery.filter(_.id === linkId)
            .map(l => (l.jsonData, l.hidden, l.updatedAt, l.storyId))
            .update(
              (JsonHelper.serialize(newLinkData),
                hidden.getOrElse(l.hidden),
                now,
                storyId.getOrElse(l.storyId))
            )
          Task.fromScalaDeferred(db.run(action))
        case Some(l: VideoLinkJsonModel) =>
          val newLinkData = l.linkData.copy(
            thumbnail = newThumbnail.orElse(l.linkData.thumbnail),
            description = newDescription.getOrElse(l.linkData.description),
            title = newTitle.getOrElse(l.linkData.title))
          val action = Links.tableQuery.filter(_.id === linkId)
            .map(l => (l.jsonData, l.hidden, l.updatedAt, l.storyId))
            .update(
              (JsonHelper.serialize(newLinkData),
                hidden.getOrElse(l.hidden),
                now,
                storyId.getOrElse(l.storyId)))
          Task.fromScalaDeferred(db.run(action))
        case Some(l: TweetLinkJsonModel) =>
          val action = Links.tableQuery.filter(_.id === linkId)
            .map(l => (l.hidden, l.updatedAt))
            .update((hidden.getOrElse(l.hidden), now))
          Task.fromScalaDeferred(db.run(action))
        case _ => Task(0)
      }.flatMap { _ =>
        Stories.getStoryById(storyId.get, env).flatMap {
          case Some(story) => Datastore.cacheStory(story)(env.redis).map(_ => 1)
          case _ => Task.now(1)
        }
      }
    } else {
      Task(0)
    }
  }

  def findByStoryId(id: StoryId, includeHidden: Boolean = false)(implicit ec: ExecutionContext) =
    Reader[DBDef, Task[List[LinkJsonModel]]] { db =>
      val q = if (includeHidden) {
        Links.tableQuery.filter(link => link.storyId === id)
      } else {
        Links.tableQuery.filter(link => link.storyId === id && !link.hidden)
      }
      val res = q.result
      for {
        entities <- Task.fromScalaDeferred(db.run(res).map(_.toList))
        models <- entities.map(_.toJson(db)).sequenceU
      } yield models
    }

  def findById(id: LinkId)(implicit ec: ExecutionContext) = Reader[DBDef, Task[Option[LinkJsonModel]]] { db =>

    val q = Links.tableQuery.filter(link => link.id === id)
    val res = q.result

    for {
      entity <- Task.fromScalaDeferred(db.run(res.headOption))
      models <- entity.map(_.toJson(db)).sequence
    } yield models
  }

  def showLink(id: LinkId)(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[Option[ShowLinkJsonModel]]] { env =>
    for {
      link <- Links.findById(id).run(env.db)
      story <- link.map(l => Stories.getStoryById(l.storyId, env)).getOrElse(Task.now(None))
    } yield link.flatMap(l => story.map(s => ShowLinkJsonModel(l, s.copy(links = s.links.filterNot(_.id == l.id)))))
  }

  def insertTweet(tweet: Tweet, submittedBy: Username, storyId: Long)(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[Long]] { env =>
    val now = com.grasswire.common.nowUtcMillis
    val entity = LinkPEntity(storyId, submittedBy, now, now, hidden = false, JsonHelper.serialize(tweet), TweetLinkType(tweet.id_str).linkType)
    val query = Links.tableQuery returning Links.tableQuery.map(_.id) += entity
    for {
      _ <- ContributorsStoryLinks.insert(ContributorStoryLinkPEntity(storyId, submittedBy))(env.db)
      id <- Task.fromScalaDeferred(env.db.run(query))
      _ <- Stories.getStoryById(storyId, env).flatMap {
        case Some(story) => Datastore.cacheStory(story)(env.redis)
        case _ => Task.now(())
      }
    } yield id

  }

  def insertPlainLink(linkData: PlainLinkJsonData, submittedBy: Username, storyId: Long)(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[Long]] { env =>
    val now = com.grasswire.common.nowUtcMillis
    val entity = LinkPEntity(storyId, submittedBy, now, now, hidden = false, JsonHelper.serialize(linkData), PlainLinkType.linkType)
    val query = Links.tableQuery returning Links.tableQuery.map(_.id) += entity
    for {
      _ <- ContributorsStoryLinks.insert(ContributorStoryLinkPEntity(storyId, submittedBy))(env.db)
      id <- Task.fromScalaDeferred(env.db.run(query))
      _ <- Stories.getStoryById(storyId, env).flatMap {
        case Some(story) => Datastore.cacheStory(story)(env.redis)
        case _ => Task.now(())
      }
    } yield id
  }

  def insertVideo(videoLinkData: VideoLinkJsonData, submittedBy: Username, storyId: Long, videoLinkType: VideoLinkType)(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[Long]] { env =>
    val now = com.grasswire.common.nowUtcMillis
    val entity = LinkPEntity(storyId, submittedBy, now, now, hidden = false, JsonHelper.serialize(videoLinkData), videoLinkType.linkType)
    val query = Links.tableQuery returning Links.tableQuery.map(_.id) += entity
    for {
      _ <- ContributorsStoryLinks.insert(ContributorStoryLinkPEntity(storyId, submittedBy))(env.db)
      id <- Task.fromScalaDeferred(env.db.run(query))
      _ <- Stories.getStoryById(storyId, env).flatMap {
        case Some(story) => Datastore.cacheStory(story)(env.redis)
        case _ => Task.now(())
      }
    } yield id
  }
}

case class StoryPEntity(name: String,
  updatedBy: Username,
  summary: Option[String],
  headline: Option[String],
  coverPhoto: Option[String],
  createdAt: Long,
  updatedAt: Long,
  hidden: Boolean = false,
  rank: Int = 0,
  id: Option[Long] = None)

object StoryPEntity {

  implicit class StoryPEntityOps(storyPEntity: StoryPEntity) {
    def pure(db: DBDef) = StoryJsonModel(storyPEntity.name, storyPEntity.headline, storyPEntity.coverPhoto, storyPEntity.summary, storyPEntity.id.get, storyPEntity.createdAt, storyPEntity.updatedAt, storyPEntity.updatedBy, Nil, storyPEntity.rank, Nil, storyPEntity.hidden)
  }

}

class IosOnboarding(tag: Tag) extends Table[(String, Option[Long])](tag, "ios_onboarding") {
  def tweet = column[String]("tweet")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * = (tweet, id.?)
}

object IosOnboarding {
  def tableQuery = TableQuery[IosOnboarding]
  def getCurrentOnboardingTweet(implicit ec: ExecutionContext): ReaderT[Task, DBDef, Option[Tweet]] =
    Kleisli[Task, DBDef, Option[Tweet]] { db =>
      import com.grasswire.common.Implicits.TaskPimps
      val q = IosOnboarding.tableQuery.sortBy(_.id.desc).take(1)
      Task.fromScalaDeferred(db.run(q.result.headOption)).map(t => t.flatMap(res => Json.parse(res._1).validate[Tweet].asOpt))
    }

  def insert(tweet: Tweet)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, Int] =
    Kleisli[Task, DBDef, Int] { db =>
      import com.grasswire.common.Implicits.TaskPimps
      val q = IosOnboarding.tableQuery += Tuple2(JsonHelper.serialize(tweet), None)
      Task.fromScalaDeferred(db.run(q))

    }
}

case class LivePagePEntity(title: String, videoEmbedUrl: String, createdBy: Username, createdAt: Long, id: Option[Long] = None)

class LivePages(tag: Tag) extends Table[LivePagePEntity](tag, "live_pages") {
  def title = column[String]("title")
  def videoEmbedUrl = column[String]("video_embed_url")
  def createdBy = column[String]("created_by")
  def createdAt = column[Long]("created_at")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * = (title, videoEmbedUrl, createdBy, createdAt, id.?) <> (LivePagePEntity.apply _ tupled, LivePagePEntity.unapply)
}

object LivePages {
  def tableQuery = TableQuery[LivePages]

  def insert(entity: LivePagePEntity)(db: DBDef)(implicit ec: ExecutionContext): Task[Int] = {
    import com.grasswire.common.Implicits.TaskPimps
    val q = LivePages.tableQuery += entity
    Task.fromScalaDeferred(db.run(q))
  }

  def getCurrent(db: DBDef)(implicit ec: ExecutionContext): Task[Option[LivePageJsonModel]] = {
    import com.grasswire.common.Implicits.TaskPimps
    val q = LivePages.tableQuery.sortBy(_.id.desc).take(1)
    Task.fromScalaDeferred(db.run(q.result.headOption)).map(_.map(livepage => LivePageJsonModel(livepage.title, livepage.videoEmbedUrl, livepage.createdBy, livepage.createdAt, livepage.id.get)))
  }

}

class Stories(tag: Tag) extends Table[StoryPEntity](tag, "stories") {

  def rank = column[Int]("rank")

  def name = column[String]("story_name")

  def storyNameIndex = index("story_name_idx", name, unique = true)

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def updatedById = column[String]("updated_by")

  def summary = column[Option[String]]("summary", O.SqlType("TEXT"))

  def headline = column[Option[String]]("headline")

  def hidden = column[Boolean]("hidden", O.Default(false))

  def coverPhoto = column[Option[String]]("cover_photo")

  def createdAt = column[Long]("created_at")

  def updatedAt = column[Long]("updated_at")

  def * = (name, updatedById, summary, headline, coverPhoto, createdAt, updatedAt, hidden, rank, id.?) <> (StoryPEntity.apply _ tupled, StoryPEntity.unapply)
}

object Stories extends Logging {

  import com.grasswire.common.Implicits.TaskPimps

  def search(term: String)(implicit ec: ExecutionContext): ReaderT[Task, DBDef, List[StoryJsonModel]] =
    Kleisli[Task, DBDef, List[StoryJsonModel]] { db =>
      val q = Stories.tableQuery.filter(story => story.name.toLowerCase like s"${term.toLowerCase}%")
      Task.fromScalaDeferred(db.run(q.result)).map(stories => stories.toList.map(_.pure(db)))
    }

  def getActive(env: GWEnvironment)(implicit ec: ExecutionContext): Task[List[StoryJsonModel]] = {
    // FIXME belongs in common code somewhere

    val activeStoriesQ = Stories.tableQuery.filter(story => !story.hidden)
    val activeStoryIdsQ = activeStoriesQ.map(_.id)

    // FIXME could be more optimal using joins rather than IN, but depends on what the PostgreSQL optimizer does
    // for the moment, seems better to keep it composable
    def usersFromSubquery[C[_]](usernames: Query[Rep[String], _, C]) =
      Users.tableQuery.filter(_.twitterScreenName in usernames)
        .joinLeft(Admins.tableQuery).on(_.twitterScreenName === _.username)

    val recentLinksQ =
      Links.tableQuery
        .filter(!_.hidden)
        .filter(_.storyId in activeStoryIdsQ)
        .map(link =>
          (link, PostgresFunctions.rankOver(
            link.storyId,
            link.createdAt,
            descending = true)))
        .subquery
        .filter { case (_, rank) => rank <= 4 }
        .map { case (link, _) => link }

    val contributionsQ =
      ContributorsStoryLinks.tableQuery
        .filter(_.storyId in activeStoryIdsQ).map { case e => (e.storyId, e.contributor) }

    val allContributorsQ =
      usersFromSubquery(contributionsQ.map { case (_, contributor) => contributor })

    val recentLinksQCompiled = Compiled(recentLinksQ)
    val activeStoriesQCompiled = Compiled(activeStoriesQ)
    val activeStoryIdsQCompiled = Compiled(activeStoryIdsQ)
    val contributionsQCompiled = Compiled(contributionsQ)
    val allContributorsQCompiled = Compiled(allContributorsQ)

    val queryCompiled =
      (activeStoriesQCompiled.result zip contributionsQCompiled.result zip
        recentLinksQCompiled.result zip allContributorsQCompiled.result)

    Task.fromScalaDeferred(env.db.run(queryCompiled)).map {
      case (((stories, contributions), recentLinks), users) =>
        val usersByUsername =
          users.foldLeft[Map[String, UserJsonModel]](Map.empty) { (m, userAndAdmin) =>
            val (user, adminOpt) = userAndAdmin
            val hydratedUser = user.hydrateWithAdminOpt(adminOpt)
            m + (user.twitterScreenName -> hydratedUser)
          }

        val contributorsByStoryId =
          contributions.foldLeft[Map[Long, Vector[UserJsonModel]]](Map.empty.withDefaultValue(Vector.empty)) { (m, contribution) =>
            val (storyId, contributorUsername) = contribution
            val existingContributors = m(storyId)
            if (existingContributors.exists(_.twitterScreenName == contributorUsername)) m
            else
              usersByUsername.get(contributorUsername) match {
                case Some(user) => m + (storyId -> (existingContributors :+ user))
                case None =>
                  logger.warn(s"when fetching active stories found a contributor $contributorUsername but that user wasn't returned with the involved users for active stories")
                  m
              }
          }

        val recentLinksByStoryId =
          recentLinks.foldLeft[Map[Long, Vector[LinkJsonModel]]](Map.empty.withDefaultValue(Vector.empty)) { (m, link) =>
            usersByUsername.get(link.submittedBy) match {
              case Some(user) =>
                val hydratedLink = link.toJsonPure(user)
                m + (link.storyId -> (m(link.storyId) :+ hydratedLink))
              case None =>
                logger.warn(s"when fetching active stories found a link submitted by ${link.submittedBy} but that user wasn't returned with the involved users for active stories")
                m
            }
          }

        stories.map { story =>
          StoryJsonModel(story.name, story.headline, story.coverPhoto, story.summary, story.id.get, story.createdAt, story.updatedAt, story.updatedBy, recentLinksByStoryId(story.id.get).toList, story.rank, contributorsByStoryId(story.id.get).toList, story.hidden)
        }.toList
    }
  }

  def getStoryById(id: Long, env: GWEnvironment)(implicit ec: ExecutionContext): Task[Option[StoryJsonModel]] = {

    val activeStoriesQ = Stories.tableQuery.filter(_.id === id)

    // FIXME could be more optimal using joins rather than IN, but depends on what the PostgreSQL optimizer does
    // for the moment, seems better to keep it composable
    def usersFromSubquery[C[_]](usernames: Query[Rep[String], _, C]) =
      Users.tableQuery.filter(_.twitterScreenName in usernames)
        .joinLeft(Admins.tableQuery).on(_.twitterScreenName === _.username)

    val recentLinksQ =
      Links.tableQuery
        .map(link => (link, PostgresFunctions.rankOver(link.storyId, link.createdAt, descending = true)))
        .subquery
        .filter { case (link, rank) => rank <= 35 && !link.hidden }
        .map { case (link, _) => link }

    val contributionsQ =
      ContributorsStoryLinks.tableQuery.filter(_.storyId === id).map { case e => (e.storyId, e.contributor) }

    val allContributorsQ =
      usersFromSubquery(contributionsQ.map { case (_, contributor) => contributor })

    val query = activeStoriesQ.result zip contributionsQ.result zip recentLinksQ.result zip allContributorsQ.result

    logger.info(s"${recentLinksQ.result.statements}")

    Task.fromScalaDeferred(env.db.run(query)).map {
      case (((stories, contributions), recentLinks), users) =>
        val usersByUsername =
          users.foldLeft[Map[String, UserJsonModel]](Map.empty) { (m, userAndAdmin) =>
            val (user, adminOpt) = userAndAdmin
            val hydratedUser = user.hydrateWithAdminOpt(adminOpt)
            logger.info(s"received user ${user.twitterScreenName}")
            m + (user.twitterScreenName -> hydratedUser)
          }

        val contributorsByStoryId =
          contributions.foldLeft[Map[Long, Vector[UserJsonModel]]](Map.empty.withDefaultValue(Vector.empty)) { (m, contribution) =>
            val (storyId, contributorUsername) = contribution
            val existingContributors = m(storyId)
            if (existingContributors.exists(_.twitterScreenName == contributorUsername)) m
            else
              usersByUsername.get(contributorUsername) match {
                case Some(user) => m + (storyId -> (existingContributors :+ user))
                case None =>
                  logger.warn(s"when fetching active stories found a contributor $contributorUsername but that user wasn't returned with the involved users for active stories")
                  m
              }
          }

        val recentLinksByStoryId =
          recentLinks.foldLeft[Map[Long, Vector[LinkJsonModel]]](Map.empty.withDefaultValue(Vector.empty)) { (m, link) =>
            usersByUsername.get(link.submittedBy) match {
              case Some(user) =>
                val hydratedLink = link.toJsonPure(user)
                m + (link.storyId -> (m(link.storyId) :+ hydratedLink))
              case None =>
                logger.warn(s"when fetching active stories found a link submitted by ${link.submittedBy} but that user wasn't returned with the involved users for active stories")
                m
            }
          }

        stories.map { story =>
          StoryJsonModel(story.name, story.headline, story.coverPhoto, story.summary, story.id.get, story.createdAt, story.updatedAt, story.updatedBy, recentLinksByStoryId(story.id.get).toList, story.rank, contributorsByStoryId(story.id.get).toList, story.hidden)
        }.headOption
    }
  }

  def updateOrdering(updatedBy: Username, orderings: List[StoryOrderingJsonModel])(implicit ec: ExecutionContext): Reader[GWEnvironment, Task[List[StoryJsonModel] \/ List[StoryJsonModel]]] = Reader[GWEnvironment, Task[List[StoryJsonModel] \/ List[StoryJsonModel]]] { env =>
    val db = env.db
    val redis = env.redis
    val now = com.grasswire.common.nowUtcMillis
    val activeStoriesQuery: Query[Stories, StoryPEntity, Seq] = Stories.tableQuery.filter(story => !story.hidden)
    val ongoingStories = Task.fromScalaDeferred(db.run(activeStoriesQuery.result)).run.toList.map(_.pure(db))
    val validUpdate: Boolean = orderings.sortBy(_.storyId).map(_.storyId) == ongoingStories.sortBy(_.id).map(_.id) && orderings.map(_.position).toSet.size == orderings.map(_.position).size
    if (validUpdate) {
      val actions = orderings.map(o => Stories.tableQuery.filter(_.id === o.storyId).map(story => (story.rank, story.updatedById, story.updatedAt)).update((o.position, updatedBy, now)))
      Task.fromScalaDeferred(db.run(DBIO.sequence(actions))).flatMap(_ => Task.fromScalaDeferred(db.run(activeStoriesQuery.result).map(_.toList.map(_.pure(db)))).flatMap(stories => Datastore.flushActiveStories(stories.map(_.id))(env.redis).map(_ => stories))).map(\/-(_))
    } else {
      Task(-\/(ongoingStories))
    }
  }

  def tableQuery = TableQuery[Stories]

  def insert(entity: StoryPEntity)(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[(Long, Int)]] { env =>
    val db = env.db

    val query = for {
      highRank <- Stories.tableQuery.filter(story => !story.hidden).sortBy(_.rank.desc).map(_.rank).result.headOption
      storyId <- Stories.tableQuery returning Stories.tableQuery.map(_.id) += entity.copy(rank = highRank.getOrElse(0) + 1)
      _ <- Changelogs.tableQuery += ChangelogPersistEntity(entity.updatedBy, s"created story ${entity.name}", storyId, None, None)
      _ <- StoryChangelogs.tableQuery += StoryChangelogPEntity(name = entity.name, updatedBy = entity.updatedBy, summary = entity.summary, coverPhoto = entity.coverPhoto, headline = entity.headline, hidden = entity.hidden, storyId = storyId)
      _ <- ContributorsStoryLinks.tableQuery += ContributorStoryLinkPEntity(storyId, entity.updatedBy)

    } yield (storyId, highRank)

    for {
      result <- Task.fromScalaDeferred(db.run(query))
      (storyId, rank) = result
      story <- Stories.getStoryById(storyId, env)
      _ <- story match {
        case Some(s) => Datastore.cacheStory(s)(env.redis)
        case _ => Task.now(())
      }
    } yield (storyId, rank.getOrElse(0) + 1)
  }

  def update(storyId: StoryId, updatedBy: Username, newName: Option[String], newHeadline: Option[String], newSummary: Option[String], newCoverPhoto: Option[String], hidden: Option[Boolean])(implicit ec: ExecutionContext) = Reader[GWEnvironment, Task[StoryEdits]] { env =>
    val db = env.db
    val now = com.grasswire.common.nowUtcMillis
    val storyEntity = Stories.tableQuery.filter(_.id === storyId)
    val nameUpdate = newName.map(n => storyEntity.map(_.name).update(n))
    val headlineUpdate = newHeadline.map(h => storyEntity.map(_.headline).update(Some(h)))
    val summaryUpdate = newSummary.map(s => storyEntity.map(_.summary).update(Some(s)))
    val coverPhotoUpdate = newCoverPhoto.map(c => storyEntity.map(_.coverPhoto).update(Some(c)))
    val hiddenUpdate = hidden.map(h => storyEntity.map(_.hidden).update(h))

    var edits = Seq[String]()

    def nameLog(oldStory: StoryPEntity) = nameUpdate.map { _ =>
      val msg = s"""updated story "${oldStory.name}" name to "${newName.get}" """
      edits = edits :+ msg
      Changelogs.tableQuery += ChangelogPersistEntity(updatedBy, msg, storyId, None, None)
    }
    def headlineLog(oldStory: StoryPEntity) = headlineUpdate.map { _ =>
      val msg = s"""updated story "${oldStory.name}" headline to "${newHeadline.get}". Previously: "${oldStory.headline.getOrElse("n/a")}" """
      edits = edits :+ msg
      Changelogs.tableQuery += ChangelogPersistEntity(updatedBy, msg, storyId, None, None)
    }
    def summaryLog(oldStory: StoryPEntity) = summaryUpdate.map { _ =>
      val msg = s"""updated a story "${oldStory.name}" summary to "${newSummary.get}". Previously: "${oldStory.summary.getOrElse("n/a")}" """
      edits = edits :+ msg
      Changelogs.tableQuery += ChangelogPersistEntity(updatedBy, msg, storyId, None, None)
    }
    def coverPhotoLog(oldStory: StoryPEntity) = coverPhotoUpdate.map { _ =>
      val msg = s"""updated story "${oldStory.name}" cover photo to "${newCoverPhoto.get}". Previously: "${oldStory.coverPhoto.getOrElse("n/a")}" """
      edits = edits :+ msg
      Changelogs.tableQuery += ChangelogPersistEntity(updatedBy, msg, storyId, None, None)
    }
    def hiddenLog(oldStory: StoryPEntity) = hiddenUpdate.map { _ =>
      val msg = s"""hid story "${oldStory.name}" """
      edits = edits :+ msg
      Changelogs.tableQuery += ChangelogPersistEntity(updatedBy, msg, storyId, None, None)
    }

    val updateActions: List[FixedSqlAction[Int, NoStream, Write]] = List(nameUpdate, headlineUpdate, summaryUpdate, coverPhotoUpdate, hiddenUpdate).flatten
    val actions = if (updateActions.nonEmpty) updateActions ++ List(storyEntity.map(_.updatedById).update(updatedBy), storyEntity.map(_.updatedAt).update(now)) else updateActions
    val updateResult = for {
      materialized <- Task.fromScalaDeferred(db.run(storyEntity.result.head))
      logActions = List(nameLog(materialized), headlineLog(materialized), summaryLog(materialized), coverPhotoLog(materialized), hiddenLog(materialized))
      n <- Task.fromScalaDeferred(db.run(DBIO.sequence(actions ++ logActions.flatten))).map(_.sum)
      _ <- Task.fromScalaDeferred(db.run(ContributorsStoryLinks.tableQuery += ContributorStoryLinkPEntity(storyId, updatedBy)))
        .handle { case t: Throwable if t.getMessage.toLowerCase.contains("duplicate key value violates unique constraint") => () }

      _ <- Stories.getStoryById(storyId, env).flatMap {
        case Some(story) => Datastore.cacheStory(story)(env.redis)
        case _ => Task.now(())
      }
    } yield n

    updateResult.flatMap { r =>
      val q = Stories.tableQuery.filter(_.id === storyId)
      Task.fromScalaDeferred(db.run(q.result.headOption)).flatMap {
        case Some(s) => StoryChangelogs.insertStoryChange(s)(db).map(_ => StoryEdits(edits))
        case _ => Task(r).map(_ => StoryEdits(edits))
      }
    }
  }
}
