package filters

import play.api.Logger
import play.api.mvc._
import scala.concurrent.Future
import play.mvc.Results._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object HTTPSRedirectFilter extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    //play uses lower case headers.
    requestHeader.headers.get("x-forwarded-proto") match {
      case Some(header) => {
        if (requestHeader.path == "/live" || header == "https") {
          nextFilter(requestHeader).map { result =>
            result
          }
        } else {
          Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
        }
      }
      case None => nextFilter(requestHeader)
    }
  }
}
