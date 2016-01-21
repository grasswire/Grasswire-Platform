package com.grasswire.api.http.routing

import akka.actor.{ ActorSystem, ActorRef }
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.LivePages
import com.grasswire.common.json_models.LivePageJsonModel
import play.api.libs.json.JsObject
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport

class LivePagesRouter(dal: DAL)(implicit system: ActorSystem) extends GWDirectives with PlayJsonSupport {

  import com.grasswire.api.http.MarshallersV1._
  import spray.routing._
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  val route: Route = {
    pathPrefix("v1") {
      path("live") {
        getCurrentLivePage
      }
    }
  }

  def getCurrentLivePage = get {
    cache(routeCache(timeToLive = 15.seconds)) {
      complete {
        LivePages.getCurrent(dal.db).map {
          case None => (StatusCodes.NotFound, None: Option[LivePageJsonModel])
          case Some(livepage) => (StatusCodes.OK, Some(livepage): Option[LivePageJsonModel])
        }
      }
    }
  }
}
