package com.grasswire.api.http.routing

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.logging.Logging

class BaseHttpService(implicit system: ActorSystem) extends GWDirectives with Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  val route = {
    logRequestResponse(methodUriStatusAsInfoLevel _) {
      path("healthcheck") {
        get {
          detach() {
            complete {
              "healthy"
            }
          }
        }
      }
    }
  }
}
