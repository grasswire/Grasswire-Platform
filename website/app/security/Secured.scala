package security

import play.api.mvc._

import scala.concurrent.Future
import services.SessionService.SessionOps


/**
 * Created by levinotik on 1/15/15.
 */

import controllers._
import play.api.mvc.Security.Authenticated

trait Secured {

  def username(request: RequestHeader) = request.session.toGWSession.map(_.user.twitterScreenName)

  def admin(request: RequestHeader) = request.session.get("is_admin").filter(_.toBoolean == true) match {
    case None => None
    case Some(_) => request.session.get(Security.username)
  }

  def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.Application.index(None))
  def onUserUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.Application.index(Some("login_required")))




  def withAdminAuth(f: => String => Request[AnyContent] => Future[Result]) = {
    Security.Authenticated[String](admin, onUnauthorized) { user =>
      Action.async(request => f(user)(request))
    }
  }

  def withAuth(f: => String => Request[AnyContent] => Future[Result]) = {
    Security.Authenticated[String](username, onUserUnauthorized) { user =>
      Action.async(request => f(user)(request))
    }
  }
}
