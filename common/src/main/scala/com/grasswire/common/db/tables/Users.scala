package com.grasswire.common.db.tables

import com.grasswire.common._
import com.grasswire.common.db.migrations.GWMigration
import com.grasswire.common.db.{ DAL, GWDatabaseDriver }
import GWDatabaseDriver.api._
import com.grasswire.common.json_models.UserJsonModel
import com.grasswire.common.models.TwitterUser
import org.joda.time.{ DateTimeZone, DateTime }
import slick.lifted
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.Try
import scalaz.concurrent.Task
import com.grasswire.common.logging.Logging
import com.grasswire.common.json_models.UserProfileJsonModel

class Users(tag: Tag) extends Table[UserPersistEntity](tag, "twitter_users") {

  def twitterScreenName = column[String]("twitter_screen_name")

  def twitterName = column[String]("twitter_name")

  def twitterId = column[Long]("twitter_user_id")

  def profileImageUrl = column[String]("profile_image_url")

  def email = column[String]("email")

  def karma = column[Int]("karma")

  def createdAt = column[Long]("created_at")

  def isHellBanned = column[Boolean]("is_hell_banned")

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def * = (twitterScreenName, twitterName, twitterId, profileImageUrl, id.?, email.?, karma, createdAt, isHellBanned) <> (UserPersistEntity.apply _ tupled, UserPersistEntity.unapply)

}

object Users extends Logging {

  import scalaz.std.list._
  import scalaz.syntax.traverse._
  import com.grasswire.common.Implicits.TaskPimps

  def findProfile(username: Username)(db: DBDef)(implicit ec: ExecutionContext): Task[Option[UserProfileJsonModel]] = {

    val userq = Users.tableQuery.filter(_.twitterScreenName.toLowerCase === username.toLowerCase)
      .joinLeft(Admins.tableQuery).on(_.twitterScreenName.toLowerCase === _.username.toLowerCase)

    val allLinks = Links.tableQuery.filter(_.submittedById.toLowerCase === username.toLowerCase)
      .map { link => (link.storyId, link.submittedById) }

    val contributionsQ =
      StoryChangelogs.tableQuery.filter(_.updatedBy.toLowerCase === username.toLowerCase)
        .map { changelog => (changelog.storyId, changelog.updatedBy.toLowerCase) } union allLinks

    def storiesFromSubquery[C[_]](ids: Query[Rep[Long], _, C]) =
      Stories.tableQuery.filter(_.id in ids)

    val allStoryContribs = storiesFromSubquery(contributionsQ.map { case (storyId, _) => storyId })

    val megaQuery = userq.result zip allStoryContribs.result

    import com.grasswire.common.json_models.StoryJsonModel
    import com.grasswire.common.json_models.UserProfileJsonModel

    Task.fromScalaDeferred {
      db.run(megaQuery).map {
        case (user, stories) =>
          val maybeUser = user.headOption.map { case (u, admin) => u.hydrateWithAdminOpt(admin) }
          val storyModels = stories.map(story => StoryJsonModel(story.name, story.headline, story.coverPhoto, story.summary, story.id.get, story.createdAt, story.updatedAt, story.updatedBy, Nil, story.rank, Nil, story.hidden))
          maybeUser.map(u => UserProfileJsonModel(u, storyModels.toList))
      }
    }
  }

  def findMany(usernames: List[Username])(db: DBDef)(implicit ec: ExecutionContext): Task[List[UserJsonModel]] = {
    import GWDatabaseDriver.api._
    import com.grasswire.common.db.tables.UserPersistEntity.UserModelOps
    val q = Users.tableQuery.filter(user => user.twitterScreenName inSet usernames)
    for {
      us <- Task.fromScalaDeferred(db.run(q.result))
      h <- us.map(_.hydrate(db)).toList.sequenceU
    } yield h
  }

  def adjustKarma(username: String, rep: Int)(db: DBDef)(implicit ec: ExecutionContext) = {

    val q = for {
      u <- com.grasswire.common.db.tables.Users.tableQuery if u.twitterScreenName === username
    } yield u.karma

    Task.fromScalaDeferred(db.run(q.result.headOption)).flatMap {
      case Some(k) =>
        val i: Int = k + rep
        val newRep = if (i < 1) 1 else i
        Task.fromScalaDeferred(db.run(q.update(newRep)))
      case None => Task.now(0)
    }.run

  }

  def getKarma(username: Username)(db: DBDef)(implicit ec: ExecutionContext) =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === username).map(_.karma).result
      .headOption)).run

  def updateEmail(username: Username, email: String)(db: DBDef)(implicit ec: ExecutionContext): Int =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === username).map(_.email).update(email))).run

  def hellBanned(username: Username)(db: DBDef)(implicit ec: ExecutionContext): Boolean =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === username).map(_.isHellBanned)
      .result.headOption).map(_.getOrElse(true))).run

  def hellBan(username: Username)(db: DBDef)(implicit ec: ExecutionContext): Int =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === username).map(_.isHellBanned)
      .update(true))).run

  def unhellBan(username: Username)(db: DBDef)(implicit ec: ExecutionContext): Int =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === username).map(_.isHellBanned)
      .update(false))).run

  def search(username: Username)(db: DBDef)(implicit ec: ExecutionContext): Task[Seq[UserJsonModel]] = {
    import GWDatabaseDriver.api._
    import com.grasswire.common.Implicits.TaskPimps
    val q = Users.tableQuery.filter(user => user.twitterScreenName.toLowerCase like s"${username.toLowerCase}%")
    for {
      us <- Task.fromScalaDeferred(db.run(q.result))
      h <- us.map(_.hydrate(db)).toList.sequenceU
    } yield h
  }

  def find(username: Username, caseInsensitive: Boolean = false)(db: DBDef)(implicit ec: ExecutionContext) =
    {

      val q = if (caseInsensitive)
        Users.tableQuery.filter(_.twitterScreenName.toLowerCase === username.toLowerCase)
      else Users.tableQuery.filter(_.twitterScreenName === username)

      Task.fromScalaDeferred(db.run(q.result.headOption)).run
    }

  def tableQuery = TableQuery[Users]

  def createNewUser(user: UserPersistEntity)(db: DBDef)(implicit ec: ExecutionContext): UserPersistEntity = {
    //TODO setup following/followers

    val id = Users.tableQuery returning Users.tableQuery.map(_.id) += user
    Task.fromScalaDeferred(db.run(id)).map(id => user.copy(id = Some(id))).run

  }

  def findUser(twitterScreenName: String)(db: DBDef)(implicit ec: ExecutionContext): Option[UserPersistEntity] =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === twitterScreenName).result
      .headOption)).run

  def updateProfileImageUrl(twitterUser: TwitterUser)(db: DBDef)(implicit ec: ExecutionContext): Int =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === twitterUser.screen_name).map(_
      .profileImageUrl).update(twitterUser.profile_image_url_https))).run

  def updateTwitterName(twitterUser: TwitterUser)(db: DBDef)(implicit ec: ExecutionContext): Int =
    Task.fromScalaDeferred(db.run(Users.tableQuery.filter(_.twitterScreenName === twitterUser.screen_name).map(_
      .twitterName).update(twitterUser.name))).run

}

case class UserPersistEntity(twitterScreenName: String,
  twitterName: String,
  twitterUserId: Long,
  profileImageUrl: String,
  id: Option[Long] = None,
  email: Option[String],
  karma: Int = 1,
  createdAt: Long = DateTime.now(DateTimeZone.UTC).getMillis,
  hellBanned: Boolean = false)

object UserPersistEntity {
  def newUser(twitterUser: TwitterUser, email: Option[String]) = UserPersistEntity(twitterUser.screen_name, twitterUser.name, twitterUser.id, twitterUser.profile_image_url_https, email = email)

  implicit class UserModelOps(entity: UserPersistEntity) {
    def hydrate(db: DBDef): Task[UserJsonModel] =
      Admins.isAdmin(entity.twitterScreenName)(db).map(hydratePure)

    def hydrateWithAdminOpt[A](admin: Option[A]): UserJsonModel =
      hydratePure(Admins.isSuperAdmin(entity.twitterScreenName) || admin.isDefined)

    def hydratePure(isAdmin: Boolean): UserJsonModel =
      UserJsonModel(entity.twitterScreenName, entity.twitterName, entity.email, entity.karma,
        entity.id.get, entity.createdAt, entity.hellBanned, isAdmin, entity.profileImageUrl)
  }

}
