package controllers

import com.grasswire.common.models.{Session => GWSession}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.oauth._
import play.api.libs.ws.WS
import play.api.mvc._
import services.SessionService
import scala.language.postfixOps
import scala.concurrent.Await
import com.grasswire.common.CommonConfig
import api.Api

object TwitterAuthController extends Controller {

  val KEY = ConsumerKey(Api.twitterConsumerKey, Api.twitterConsumerSecret)
  
  val TWITTER = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authenticate", KEY),
    use10a = true)

  def authenticate(uri: String) = Action { implicit request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          Logger.info("got right access token")
          import scala.concurrent.duration._
          val responseBody = Await.result(WS.url(api.Api.loginViaTwitter(t, None)).post(Results.EmptyContent()).map(r => r.body), 5 seconds)
          Json.parse(responseBody).validate[GWSession] match {
            case JsSuccess(session, _) =>
              val httpSession = SessionService.toHttpSession(session)
              implicit val gwSession = SessionService.get(httpSession)
              if (session.isFirstLogin) {
                Logger.info("is first login")
                if (uri == "/new-story") {
                  Redirect(routes.Application.newStory(Some("signup_success"))).withSession(httpSession)
                } else {
                  Redirect(routes.Application.edit(Some("signup_success"), None)).withSession(httpSession)
                }
              } else {
                Logger.info("is not first login")
                if (uri == "/new-story") {
                  Redirect(routes.Application.newStory(None)).withSession(httpSession)
                } else {
                  Redirect(routes.Application.edit(None, None)).withSession(httpSession)
                }
              }
            case _ => Ok("uh oh, something went wrong")
          }
        }
        case Left(e) =>
          Logger.info("got left, throwing e")
          throw e
      }
    }.getOrElse(
        request.getQueryString("denied") match {
          case Some(_) => Redirect(routes.Application.index(None)).withNewSession
          case _ => TWITTER.retrieveRequestToken(routes.TwitterAuthController.authenticate(uri).absoluteURL()) match {
            case Right(t) =>
              Logger.info("get or else right t")
              Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
            case Left(e) => throw e
          }
        })
  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }
}
