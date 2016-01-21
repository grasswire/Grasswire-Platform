package api

import com.grasswire.common.AuthHeaders
import com.grasswire.common.models.{Session => GWSession}
import com.typesafe.config.{Config, ConfigFactory}
import controllers.AjaxController._
import play.api.Application
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.oauth.RequestToken
import play.api.libs.ws.{WS, WSRequestHolder, WSResponse}
import play.api.mvc._
import services.SessionService
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scalaz.{-\/, \/, \/-}

object Api {

  import Implicits._

  def signRequest[A](apiRequest: WSRequestHolder)(f: WSRequestHolder => Future[Result])(implicit req: Request[A], executionContext: ExecutionContext) = {
    apiRequest.withAuthHeaders(req.session).fold(l => Future(Forbidden), r => f(r))
  }


  type Username = String
  type ContentId = java.util.UUID
  type Slug = String

  val grasswireConfig: Config = ConfigFactory.load().getConfig("grasswire")
  val endpoint = grasswireConfig.getString("host") + grasswireConfig.getString("apiVersion")


  def postJsonGetJsonAction[A: Reads : Writes](holder: WSRequestHolder)(implicit ec: ExecutionContext): Action[JsValue] = Action.async(BodyParsers.parse.json) { implicit req =>
    holder.withAuthHeaders(req.session).fold(l => Future {
      Forbidden
    },
      r => req.body.validate[A].fold(l1 => Future {
        BadRequest
      },
        r1 => {
          val stringified: String = Json.toJson(r1).toString()
          r.withHeaders("Content-Type" -> "application/json").post(stringified).map { r => new Status(r.status)(r.body).as("application/json") }
        })
    )
  }

  def postEmptyContentAction(holder: WSRequestHolder)(implicit ec: ExecutionContext): Action[AnyContent] = Action.async { implicit req =>
    holder.withAuthHeaders(req.session).fold(l => Future {
      Forbidden
    }, r => r.post(Results.EmptyContent()).map { r => new Status(r.status)(r.body).as("application/json") }
    )
  }


  def createStory(implicit application: Application) = {
    WS.url(apiEndpoint("stories"))
  }

  def editStory(id: Long)(implicit application: Application) = {
    WS.url(apiEndpoint("stories")).withQueryString("story_id" -> id.toString)
  }

  def editStoryOrdering = apiEndpoint("stories/ordering")

  // 'link_id.as[Long], 'story_id.as[Long] ?, 'thumbnail ?, 'title.?, 'description.?, 'hidden.as[Boolean] ?
  def editLink(storyId: Long, linkId: Long, thumbnail: Option[String], title: Option[String], description: Option[String], hidden: Option[Boolean])(implicit application: Application) = {
    val qParams = List(("link_id", Some(linkId.toString)), ("story_id", Some(storyId.toString)), ("thumbnail", thumbnail), ("title", title), ("description", description), ("hidden", hidden.map(_.toString))).filter(_._2.nonEmpty).map(t => (t._1, t._2.get))
    WS.url(apiEndpoint("links")).withQueryString(qParams: _*)
  }

  def getActiveStories = apiEndpoint("stories")

  def lookupStory(storyId: Long)(implicit application: Application) = WS.url(apiEndpoint("stories/lookup")).withQueryString("id" -> storyId.toString)

  def showLink(id: Long, includeStory: Boolean = true)(implicit application: Application) =
    WS.url(apiEndpoint("links/show")).withQueryString("id" -> id.toString, "include_story" -> includeStory.toString)

  def hideStory(id: Long)(implicit application: Application) = WS.url(apiEndpoint("stories")).withQueryString("story_id" -> id.toString, "hidden" -> true.toString)

  def unarchiveStory(id: Long)(implicit application: Application) = WS.url(apiEndpoint("tags/unarchive")).withQueryString("storyId" -> id.toString)

  def createDigest(implicit application: Application) = WS.url(apiEndpoint("digests"))

  def searchStories(query: String)(implicit application: Application) = WS.url(apiEndpoint("stories/search")).withQueryString("q" -> query)

  def searchUsers(query: String)(implicit application: Application) = WS.url(apiEndpoint("users/search")).withQueryString("q" -> query)

  def lockdown(locked: Boolean)(implicit application: Application) = WS.url(apiEndpoint("admin/lockdown")).withQueryString("locked" -> locked.toString)

  def lockdownStatus(implicit application: Application) = WS.url(apiEndpoint("admin/lockdown"))

  def makeAdmin(username: String)(implicit application: Application) = WS.url(apiEndpoint("admin/admins")).withQueryString("username" -> username)

  def listDigests(offset: Int, limit: Int)(implicit application: Application) = WS.url(apiEndpoint("digests/list")).withQueryString("limit" -> limit.toString, "offset" -> offset.toString)

  def createLivePage(implicit application: Application) = WS.url(apiEndpoint("admin/live"))

  def getLivePage(implicit application: Application) = WS.url(apiEndpoint("live"))

  val links = apiEndpoint("links")

  def linkPreview(url: String)(implicit application: Application) =
    WS.url("http://scrapey.grasswire.com/link_preview").withQueryString("url" -> url)


  val showLink = apiEndpoint("links/show")
  val showTweet = apiEndpoint("tweets/show")

  def loginViaTwitter(token: RequestToken, email: Option[String]) = email match {
    case Some(e) => s"$endpoint/sessions/twitter?token=${token.token}&secret=${token.secret}&email=$email"
    case None => s"$endpoint/sessions/twitter?token=${token.token}&secret=${token.secret}"
  }

  def changeLogs(offset: Int, max: Int)(implicit application: Application) = WS.url(apiEndpoint("change_logs")).withQueryString("offset" -> offset.toString, "max" -> max.toString)

  def storyChangeLogs(id: Long, offset: Int, max: Int)(implicit application: Application) = WS.url(apiEndpoint("change_logs/stories")).withQueryString("id" -> id.toString, "offset" -> offset.toString, "max" -> max.toString)

  def revertStory(storyId: Long, version: Long)(implicit application: Application) = WS.url(apiEndpoint("stories/revert")).withQueryString("storyId" -> storyId.toString, "version" -> version.toString)

  def unreadNotifications(username: String) = apiEndpoint(s"$username/notifications/unread")

  def markNotificationAsRead(username: String, notificationId: String) = apiEndpoint(s"$username/notifications/$notificationId/read")

  def apiEndpoint(path: String) = s"$endpoint/$path"

  def userKarma(username: String) = apiEndpoint(s"user/$username/karma")

  def userProfile(username: String)(implicit application: Application) = WS.url(apiEndpoint("users/show")).withQueryString("username" -> username)

  def markNotificationsRead(commaSepIds: String)(implicit application: Application) = WS.url(apiEndpoint("notifications")).withQueryString("ids" -> commaSepIds)

  def hellban(username: Username, ban: Boolean)(implicit application: Application) = WS.url(apiEndpoint("admin/hellban")).withQueryString("username" -> username, "ban" -> ban.toString)


  def createLink(url: String, canonicalUrl: String, thumbnail: Option[String], title: String, description: String, storyId: Long)(implicit application: Application) = {
    val baseUrl: WSRequestHolder = WS.url(apiEndpoint("links")).withQueryString("url" -> url, "canonical_url" -> canonicalUrl, "title" -> title, "description" -> description, "story_id" -> storyId.toString)
    thumbnail.map(t => baseUrl.withQueryString("thumbnail" -> t)).getOrElse(baseUrl)
  }

  def todaysContributors(implicit app: Application) = WS.url(apiEndpoint("stories/contributors/today")).withQueryString("high_quality" -> true.toString)

  def listNotifications(username: String, unreadOnly: Boolean = true, sinceId: Option[Long] = None)(implicit application: Application) = {
    val base = WS.url(apiEndpoint("notifications/list")).withQueryString("username" -> username, "unread_only" -> unreadOnly.toString)
    sinceId.map(since => base.withQueryString("since_id" -> since.toString)).getOrElse(base)
  }

  def updateEmail = apiEndpoint(s"users/update_email")

  sealed trait ApiRequestException {
    def toHttpStatus: play.api.mvc.Result = this match {
      case NoGrasswireSessionFound => Forbidden
      case e: JSValidationException => BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(e.errors)))
    }
  }

  case object NoGrasswireSessionFound extends ApiRequestException

  case class JSValidationException(errors: Seq[(JsPath, Seq[ValidationError])]) extends ApiRequestException

  case class AuthorizedRequest(authedUsername: Username, request: WSRequestHolder)

  object Implicits {

    import SessionService.SessionOps


    implicit class RichWSResponse(response: WSResponse) {
      def pipeStatusBody = new Status(response.status)(response.body)
    }

    implicit class RichRequestHolder(req: WSRequestHolder) {
      def withAuthHeaders(session: Session): Throwable \/ WSRequestHolder = {
        \/.fromTryCatchNonFatal {
          val gwSession = session.toGWSession.get
          val headers = AuthHeaders.get(gwSession.token, gwSession.user.twitterScreenName)
          req.withHeaders("digest" -> headers.digest, "timestamp" -> headers.timestamp.toString, "username" -> headers.username, "uuid" -> headers.uuid)
        }
      }

      def authorize(implicit request: Request[Any]): ApiRequestException \/ AuthorizedRequest = request.session.toGWSession match {
        case Some(sesh) =>
          val headers = AuthHeaders.get(sesh.token, sesh.user.twitterScreenName)
          \/-(AuthorizedRequest(sesh.user.twitterScreenName, req.withHeaders("digest" -> headers.digest, "timestamp" -> headers.timestamp.toString, "username" -> headers.username, "uuid" -> headers.uuid)))
        case None => -\/(NoGrasswireSessionFound)
      }

      def withContentTypeJson = req.withHeaders("Content-Type" -> "application/json")
    }

    implicit class RichJSRequest(request: Request[play.api.libs.json.JsValue]) {
      def validating[A: ClassTag : Reads]: ApiRequestException \/ A = request.body.validate[A] match {
        case JsError(errors) => -\/(JSValidationException(errors))
        case JsSuccess(value, _) => \/-(value)
      }
    }

    implicit class RichJsResult[A](result: play.api.libs.json.JsResult[A]) {
      def toEither: Seq[(JsPath, Seq[ValidationError])] \/ A = result match {
        case JsError(errors) => -\/(errors)
        case JsSuccess(value, _) => \/-(value)
      }
    }

  }

}
