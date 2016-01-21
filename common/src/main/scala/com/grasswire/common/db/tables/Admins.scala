package com.grasswire.common.db.tables

/**
 * Created by levinotik on 3/9/15.
 */

import com.grasswire.common.{ DBDef, Username }
import com.grasswire.common.db.GWDatabaseDriver
import GWDatabaseDriver.api._
import org.joda.time.{ DateTimeZone, DateTime }
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import com.grasswire.common.Implicits.TaskPimps

import scalaz.concurrent.Task

class Admins(tag: Tag) extends Table[(String, String)](tag, "gw_admins") {

  def username = column[String]("ADMIN_USERNAME", O.PrimaryKey)
  def grantedBy = column[String]("GRANTED_BY")
  def * = (username, grantedBy)

}

case class SiteLockdown(lockedDown: Boolean, timestamp: Long, updatedBy: String, id: Option[Long] = None)

class SiteLockdowns(tag: Tag) extends Table[SiteLockdown](tag, "site_lockdown") {
  def lockedDown = column[Boolean]("locked_down")
  def timestamp = column[Long]("timestamp")
  def updatedBy = column[String]("updated_by")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * = (lockedDown, timestamp, updatedBy, id.?) <> (SiteLockdown.apply _ tupled, SiteLockdown.unapply)

}

object SiteLockdowns {
  def tableQuery = TableQuery[SiteLockdowns]
  def lock(username: Username)(db: DBDef): Task[Int] = {
    val ld = SiteLockdown(lockedDown = true, DateTime.now(DateTimeZone.UTC).getMillis, username)
    val q = SiteLockdowns.tableQuery += ld
    Task.fromScalaDeferred(db.run(q))
  }
  def unlock(username: Username)(db: DBDef) = {
    val ld = SiteLockdown(lockedDown = false, DateTime.now(DateTimeZone.UTC).getMillis, username)
    val q = SiteLockdowns.tableQuery += ld
    Task.fromScalaDeferred(db.run(q))
  }

  def isLocked(db: DBDef): Task[Boolean] = {
    val q = SiteLockdowns.tableQuery.sortBy(_.id.desc).take(1)
    Task.fromScalaDeferred(db.run(q.result.headOption).map(_.exists(t => t.lockedDown)))
  }

  def getStatus(db: DBDef): Task[Option[SiteLockdown]] = {
    val q = SiteLockdowns.tableQuery.sortBy(_.id.desc).take(1)
    Task.fromScalaDeferred(db.run(q.result.headOption))
  }
}

object Admins {
  def tableQuery = TableQuery[Admins]

  def makeAdmin(username: Username, grantor: Username)(db: DBDef): Int = {
    val q = Admins.tableQuery += ((username, grantor))
    Await.result(db.run(q), 10 seconds)
  }

  def isAdmin(twitterScreenName: String)(db: DBDef): Task[Boolean] =
    if (isSuperAdmin(twitterScreenName)) Task.now(true)
    else {
      val q = Admins.tableQuery.filter(_.username === twitterScreenName)
      Task.fromScalaDeferred(db.run(q.result.headOption).map(_.nonEmpty))
    }

  def isSuperAdmin(twitterScreenName: String): Boolean =
    twitterScreenName.equalsIgnoreCase("levinotik") || twitterScreenName.equalsIgnoreCase("austenallred")
}
