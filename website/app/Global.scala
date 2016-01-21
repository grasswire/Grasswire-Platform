import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import filters.HTTPSRedirectFilter
import play.api._
import play.filters.gzip.GzipFilter
import play.api.mvc.Results._
import services.SessionService
import play.api.Logger
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import SessionService.SessionOps


object Global extends GlobalSettings {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import play.api.Play.current

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val req = Request(request, Results.EmptyContent)
    implicit val session = req.session.toGWSession

    if (play.api.Play.isDev(play.api.Play.current)) {
      super.onError(request, ex)
    } else
      Future {
        InternalServerError(
          views.html.pages.static.error_500()
        )
      }
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    implicit val req = Request(request, Results.EmptyContent)
    implicit val session = req.session.toGWSession
    if (play.api.Play.isDev(play.api.Play.current)) {
      super.onHandlerNotFound(request)
    } else
      Future {
        InternalServerError(
          views.html.pages.static.error_404()
        )
      }
  }

  val gzipFilter = new GzipFilter(shouldGzip = (request, response) =>
    response.headers.get("Content-Type").exists(_.startsWith("text/html")))

  override def doFilter(next: EssentialAction): EssentialAction = {
    if(play.api.Play.isDev(play.api.Play.current)) {
      Filters(super.doFilter(next), gzipFilter, LoggingFilter)
    } else {
      Filters(super.doFilter(next), gzipFilter, HTTPSRedirectFilter, LoggingFilter)
    }
  }

  object HTMLCompressorFilter {

    def apply() = new HTMLCompressorFilter({
      val compressor = new HtmlCompressor()
      compressor.setPreserveLineBreaks(true)
      compressor.setRemoveComments(true)
      compressor.setRemoveIntertagSpaces(false)
      compressor.setRemoveHttpProtocol(false)
      compressor.setRemoveHttpsProtocol(false)
      compressor
    })
  }
}

object LoggingFilter extends EssentialFilter {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val startTime = System.currentTimeMillis
      nextFilter(requestHeader).map { result =>
        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime
        Logger.info(s"${requestHeader.method} ${requestHeader.uri}" +
          s" took ${requestTime}ms and returned ${result.header.status}")
        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}
