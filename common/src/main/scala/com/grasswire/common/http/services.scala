package com.grasswire.common.http

import akka.event.Logging
import akka.event.Logging
import com.grasswire.common.logging.Logging
import org.slf4j.LoggerFactory
import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.routing.directives.{ LogEntry, LoggingMagnet }
import spray.util.LoggingContext
import util.control.NonFatal
import akka.actor.{ ActorLogging, Actor }

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

trait FailureHandling {
  this: HttpService =>

  def rejectionHandler: RejectionHandler = RejectionHandler.Default

  def exceptionHandler(implicit log: LoggingContext) = ExceptionHandler {

    case e: IllegalArgumentException => ctx =>
      loggedFailureResponse(ctx, e,
        message = "The server was asked a question that didn't make sense: " + e.getMessage,
        error = NotAcceptable)

      case e: NoSuchElementException => ctx =>
      loggedFailureResponse(ctx, e,
        message = "The server is missing some information. Try again in a few moments.",
        error = NotFound)

      case t: Throwable => ctx => loggedFailureResponse(ctx, t)
  }

  private def loggedFailureResponse(ctx: RequestContext,
    thrown: Throwable,
    message: String = "The server is having problems.",
    error: StatusCode = InternalServerError)(implicit log: LoggingContext): Unit = {
    log.error(thrown, ctx.request.toString)
    ctx.complete((error, message))
  }

}

class RoutedHttpService(route: Route) extends Actor with HttpService with ActorLogging with Logging {

  implicit def actorRefFactory = context

  implicit val handler = ExceptionHandler {
    case NonFatal(ErrorResponseException(statusCode, entity)) => ctx => {
      ctx.complete((statusCode, entity))
    }

    case NonFatal(e) => ctx => {
      logger.error(InternalServerError.defaultMessage, e)
      ctx.complete(InternalServerError)
    }
  }

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + " request headers: " + req.headers.mkString(" -- ") + " " + "response headers " + res.headers.mkString("--") + " " + res.message.status, akka.event.Logging.InfoLevel))
    case _ => None // other kind of responses
  }

  def routeWithLogging(route: Route) = logRequestResponse(requestMethodAndResponseStatusAsInfo _)(route)

  def receive: Receive =
    runRoute(route)(handler, RejectionHandler.Default, context, RoutingSettings.default, LoggingContext.fromAdapter(Logging.getLogger(actorRefFactory.system, this)))

}
