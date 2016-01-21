package com.grasswire.api.http.directives

import akka.event.Logging
import spray.http._
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing.Directives
import spray.routing.directives.{ CachingDirectives, LogEntry, LoggingMagnet }

trait GWDirectives extends Directives with CORSSupport with SecurityDirectives with CachingDirectives {

  def methodUriStatusAsInfoLevel(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => req.headers.find(_.is("username")) match {
      case Some(username) => Some(LogEntry(s"${req.method} ${req.uri.path} ${req.uri.query} username=${username.value} ${res.message.status}", akka.event.Logging.InfoLevel))
      case _ => Some(LogEntry(s"${req.method} ${req.uri.path} ${req.uri.query} ${res.message.status}", akka.event.Logging.InfoLevel))
    }
    case _ => None // other kind of responses
  }

  def loggingRequest =
    logRequestResponse(LoggingMagnet.forRequestResponseFromMarkerAndLevel("static" -> akka.event.Logging.ErrorLevel))

  def defaultOptions = options {
    respondWithHeaders(HttpHeaders.`Access-Control-Allow-Methods`(HttpMethods.POST :: HttpMethods.DELETE :: HttpMethods.GET :: HttpMethods.PUT :: HttpMethods.OPTIONS :: Nil) ::
      HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins) :: HttpHeaders.`Access-Control-Allow-Headers`("Content-Type" :: "username" :: "timestamp" :: "uuid" :: "digest" :: Nil) :: Nil) {
      complete((StatusCodes.OK, "Allow: OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE"))
    }
  }

  def showRequest(request: HttpRequest) = LogEntry(request.method.name.toUpperCase + " " + request.uri + " " + request.headers.filter(_.name.equalsIgnoreCase("x-forwarded-for")).mkString(","), Logging.InfoLevel)

  def completeWithLog(r: => ToResponseMarshallable) = logRequest(showRequest _) {
    complete(r)
  }

}