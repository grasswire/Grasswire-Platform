package com.grasswire.api.http.routing

import com.grasswire.api.http.directives.GWDirectives
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.Route

class EmailListsRouter extends GWDirectives with PlayJsonSupport {

  def addMember = post {
    parameters('list_id, 'email) { (listId, email) =>
      complete {
        StatusCodes.NotImplemented
      }
    }
  }

  val route: Route = pathPrefix("v1") {
    pathPrefix("lists") {
      path("members") {
        addMember
      }
    }
  }
}
