package com.grasswire.common.db.tables

import com.grasswire.common.DBDef
import com.grasswire.common.db.GWDatabaseDriver
import GWDatabaseDriver.api._
import com.grasswire.common.models.SharedSecret
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import com.grasswire.common.Implicits.TaskPimps

import scalaz.concurrent.Task

/**
 * Created by levinotik on 1/7/15.
 */
class SharedSecrets(tag: Tag) extends Table[SharedSecret](tag, "SHARED_SECRETS") {
  def username = column[String]("USER_NAME")

  def secret = column[String]("SECRET")

  def * = (username, secret) <> ((SharedSecret.apply _) tupled, SharedSecret.unapply)
}

object SharedSecrets {
  def tableQuery = TableQuery[SharedSecrets]

  def findSecret(username: String, uuid: String)(implicit ec: ExecutionContext): DBDef => Option[String] = db => {
    val q = SharedSecrets.tableQuery.filter(s => s.username === username && s.secret === uuid)
    Task.fromScalaDeferred(db.run(q.result.headOption)).map(_.map(_.token)).run
  }

  def getOrCreate(twitterScreenName: String)(implicit ec: ExecutionContext): DBDef => SharedSecret = db => {
    Task.fromScalaDeferred(db.run(SharedSecrets.tableQuery.filter(_.username === twitterScreenName).result
      .headOption)).flatMap {
      case Some(x) => Task.now(x)
      case None =>
        val newSecret = SharedSecret(twitterScreenName, java.util.UUID.randomUUID().toString)
        Task.fromScalaDeferred(db.run(SharedSecrets.tableQuery += newSecret)).map(_ => newSecret)
    }.run
  }
}
