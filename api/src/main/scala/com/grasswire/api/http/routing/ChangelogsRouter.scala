package com.grasswire.api.http.routing

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.{ StoryChangelogs, Changelogs }
import com.grasswire.common.logging.Logging
import spray.httpx.PlayJsonSupport
import spray.routing.Route

import scala.concurrent.ExecutionContext

class ChangelogsRouter(dal: DAL)(implicit ec: ExecutionContext, system: ActorSystem) extends GWDirectives with PlayJsonSupport with Logging {

  import com.grasswire.api.http.MarshallersV1._

  def listLogs = pathEndOrSingleSlash {
    get {
      parameters('offset.as[Int], 'max.as[Int]) { (offset, max) =>
        complete {
          Changelogs.list(offset, max).run(dal.db)
        }
      }

    }
  }

  def listStoryChanges = path("stories") {
    get {
      parameters('id.as[Long], 'offset.as[Int], 'max.as[Int]) { (storyId, offset, max) =>
        complete {
          StoryChangelogs.list(storyId, offset, max).run(dal.db)
        }
      }
    }
  }

  val route: Route = {
    logRequestResponse(methodUriStatusAsInfoLevel _) {
      compressResponseIfRequested() {
        pathPrefix("v1") {
          pathPrefix("change_logs") {
            listLogs ~ listStoryChanges
          }
        }
      }
    }
  }
}
