package com.grasswire.sslredirect

import akka.actor.ActorSystem
import spray.http.{ StatusCodes, HttpRequest }

import spray.routing._
import spray.routing.directives.LoggingMagnet

object SslRedirectApp extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("ssl-redirect")

  startServer("0.0.0.0", 8090) {
    logRequestResponse(LoggingMagnet.forRequestResponseFromMarkerAndLevel("static" -> akka.event.Logging.InfoLevel)) {
      requestInstance {
        case request: HttpRequest => redirect(request.uri.copy(scheme = "https"), StatusCodes.MovedPermanently)
      }
    }
  }
}
