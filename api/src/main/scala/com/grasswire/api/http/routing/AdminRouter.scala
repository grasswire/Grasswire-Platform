package com.grasswire.api.http.routing

import javax.ws.rs.Path

import akka.actor.{ ActorRef, ActorSystem }
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.api.http.security.Security.GWAuthCredentials
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.{ LivePagePEntity, LivePages, SiteLockdowns }
import com.grasswire.common.models.http.LivePagePostModel
import com.grasswire.users.PostgresUserManager
import com.wordnik.swagger.annotations._
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json.{ JsBoolean, JsObject }
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task

/**
 * Created by levinotik on 1/14/15.
 */

@Api(value = "/admin")
class AdminRouter(dal: DAL, redis: scredis.Redis)(implicit ec: ExecutionContext) extends GWDirectives with PlayJsonSupport {

  val pgUserManager = new PostgresUserManager(dal)

  import com.grasswire.api.http.MarshallersV1._
  import spray.routing._

  val route: Route = {
    pathPrefix("v1") {
      requireAdmin(dal, redis, ec) { credentials =>
        pathPrefix("admin") {
          lockdown(credentials) ~ path("hellban") {
            hellBan
          } ~ path("admins") {
            makeAdmin(credentials.username)
          } ~ path("live") {
            createLivePage(credentials)
          }
        }
      }
    }
  }

  def createLivePage(credentials: GWAuthCredentials) = post {
    entity(as[LivePagePostModel]) { livepage =>
      complete {
        LivePages.insert(LivePagePEntity(livepage.title, livepage.videoEmbedUrl, credentials.username, DateTime.now(DateTimeZone.UTC).getMillis))(dal.db)
          .map(_ => (StatusCodes.Created, JsObject(Nil)))
      }
    }
  }

  @Path("/hellban")
  @ApiOperation(value = "hellban a user", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "username", value = "Username of user to hellban", required = true, dataType = "String", paramType = "query")
  ))
  def hellBan = put {
    parameters('username, 'ban.as[Boolean]) { (username, ban) =>
      complete {
        if (ban) {
          pgUserManager.hellban(username).map(_ => (StatusCodes.OK, JsObject(Nil)))
        } else {
          pgUserManager.unhellban(username).map(_ => (StatusCodes.OK, JsObject(Nil)))
        }
      }
    }
  }

  def lockdown(creds: GWAuthCredentials) = path("lockdown") {
    post {
      parameter("locked".as[Boolean]) { locked =>
        complete {
          val q = if (locked) SiteLockdowns.lock(creds.username)(dal.db) else SiteLockdowns.unlock(creds.username)(dal.db)
          q.map(_ => (StatusCodes.Created, JsObject(Nil)))
        }
      }
    } ~ get {
      complete {
        SiteLockdowns.getStatus(dal.db).map {
          case Some(status) => (StatusCodes.OK, JsObject(Seq("locked" -> JsBoolean(status.lockedDown))))
          case _ => (StatusCodes.OK, JsObject(Seq("locked" -> JsBoolean(false))))
        }
      }
    }
  }

  @Path("/admins")
  @ApiOperation(value = "promote a user to admin", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "username", value = "Username of user to make admin", required = true, dataType = "String", paramType = "query")
  ))
  def makeAdmin(grantor: String) = post {
    parameter('username) { username =>
      complete {
        Task {
          pgUserManager.makeAdmin(username, grantor) match {
            case 1 => (StatusCodes.Created, JsObject(Nil))
            case _ => (StatusCodes.NotFound, JsObject(Nil))
          }
        }
      }
    }
  }
}
