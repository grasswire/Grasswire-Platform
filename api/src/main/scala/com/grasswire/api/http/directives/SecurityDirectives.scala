package com.grasswire.api.http.directives

/**
 * Levi Notik
 * Date: 2/25/14
 */

import com.grasswire.common.{ DBDef, Reputation }
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.{ Users, SiteLockdowns, SharedSecrets }
import com.grasswire.common.models.RedisCache
import com.grasswire.api.http.security.Security
import com.grasswire.users.PostgresUserManager
import org.slf4j.LoggerFactory
import spray.http.StatusCodes
import spray.routing._
import com.grasswire.api.http.security.Security.{ PermissionService, GWAuthCredentials }
import scala.concurrent.ExecutionContext
import scalaz.{ \/-, -\/ }
import scala.concurrent.duration._
import com.grasswire.common.Implicits.TaskPimps
import scalaz.concurrent.Task

trait SecurityDirectives extends Directives {

  private lazy val secLogger = LoggerFactory.getLogger("com.grasswire.api.http.directives.SecurityDirectives")

  def requireAdmin(implicit dal: DAL, redis: scredis.Redis, ec: ExecutionContext): Directive1[GWAuthCredentials] = {
    requireCredentialHeaders.flatMap { credentials =>
      val username: String = credentials.username
      if (username.toLowerCase == "austenallred" || username.toLowerCase == "levinotik" || username.toLowerCase ==
        "matthewkeyslive" || username.toLowerCase == "joanne_stocker" || username.toLowerCase == "mark_l_watson" ||
        new PostgresUserManager(dal).isAdmin(credentials.username).run) {
        requireUserAuth(username)
      } else {
        secLogger.info("REJECTING ADMIN CREDS")
        reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
      }
    }
  }

  def requireUserAuth(username: String)(implicit dal: DAL, redis: scredis.Redis, ec: ExecutionContext): Directive1[GWAuthCredentials] = {
    requireCredentialHeaders.flatMap { credentials =>
      if (credentials.username == username) {
        PermissionService(validate)
          .validateCredentials(Task.fromScalaDeferred(redis.get[String](RedisCache.sessionKey(credentials.username))).run.filter(key => key == credentials.uuid)
            // .validateCredentials(redis.withClient(_.get[String](RedisCache.sessionKey(credentials.username)).filter(key => key == credentials.uuid))
            .orElse(SharedSecrets.findSecret(credentials.username, credentials.uuid)(ec)(dal.db)
              .map(sessionKey => {
                redis.set(RedisCache.sessionKey(credentials.username), sessionKey, ttlOpt = Some(1.hour));
                sessionKey
              }
              )), credentials) match {
            case -\/(_) =>
              secLogger.info("rejecting user auth")
              reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
            case \/-(_) =>
              provide(credentials)
          }
      } else {
        reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
      }
    }
  }

  import scala.concurrent.duration._

  def checkSiteLock(username: String)(db: DBDef)(implicit ec: ExecutionContext): Directive0 = {
    val lockStatus = SiteLockdowns.getStatus(db).run
    lockStatus match {
      case Some(lockdown) if lockdown.lockedDown =>
        Users.findUser(username)(db) match {
          case Some(user) if user.createdAt >= lockdown.timestamp => complete((StatusCodes.Locked, "Service is currently locked due to traffic spike"))
          case Some(user) if user.createdAt < lockdown.timestamp => pass
          case _ => reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
        }
      case _ => pass
    }
  }

  def requireKarma(min: Reputation)(implicit dal: DAL, redis: scredis.Redis, ec: ExecutionContext): Directive1[GWAuthCredentials] =
    authOrReject.flatMap {
      case c => Users.find(c.username)(dal.db) match {
        case Some(user) if user.karma >= min => provide(c)
        case _ => reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
      }
    }

  def authOrReject(implicit dal: DAL, redis: scredis.Redis, ec: ExecutionContext): Directive1[GWAuthCredentials] =
    requireCredentialHeaders.flatMap { credentials =>
      if (Users.hellBanned(credentials.username)(dal.db)) {
        complete(Tuple2(StatusCodes.Accepted, "processing request"))
      } else {
        PermissionService(validate)
          .validateCredentials(Task.fromScalaDeferred(redis.get[String](RedisCache.sessionKey(credentials.username))).run.filter(key => key == credentials.uuid)
            .orElse(SharedSecrets.findSecret(credentials.username, credentials.uuid)(ec)(dal.db)
              .map(sessionKey => {
                redis.set(RedisCache.sessionKey(credentials.username), sessionKey, ttlOpt = Some(1.hour));
                sessionKey
              }
              )), credentials) match {
            case -\/(_) =>
              reject(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
            case \/-(_) =>
              provide(credentials)
          }
      }
    }

  private[this] def requireCredentialHeaders: Directive1[GWAuthCredentials] =
    (headerValueByName("username") & headerValueByName("timestamp") & headerValueByName("uuid") & headerValueByName("digest"))
      .as[GWAuthCredentials](GWAuthCredentials)

  private def validate(sharedSecret: Option[String], suppliedCredentials: GWAuthCredentials) = sharedSecret match {
    case Some(s) if Security.credentialsAreValid(s, suppliedCredentials) => \/-("valid")
    case _ =>
      -\/("invalid credentials")
  }
}
