package com.grasswire.users

import com.grasswire.common.DBDef
import com.grasswire.common.db.tables.{ Admins, SharedSecrets, Users }
import com.grasswire.common.db.{ DAL, GWDatabaseDriver }
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext
import scalaz.Monad
import scalaz.concurrent.Task

class PostgresUserManager(dal: DAL) extends UserManager[Task] {
  override def M = Monad[Task]

  def hellban(username: String)(implicit ec: ExecutionContext): Task[Int] = M.point(Users.hellBan(username)(dal.db))

  def unhellban(username: String)(implicit ec: ExecutionContext): Task[Int] = M.point(Users.unhellBan(username)(dal.db))

  def getOrCreateSecret(twitterScreenName: String)(db: DBDef)(implicit ec: ExecutionContext) =
    SharedSecrets.getOrCreate(twitterScreenName)(ec)(db)

  def makeAdmin(grantee: String, grantor: String): Int = Admins.makeAdmin(grantee, grantor)(dal.db)

  def isAdmin(username: String) = Admins.isAdmin(username)(dal.db)

}
