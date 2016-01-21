package controllers

import api.Api
import com.wordnik.swagger.annotations.{Api => SwaggerApi}
import play.api.libs.ws.WS
import play.api.mvc._
import Api.signRequest
import services.SessionService.SessionOps

import scala.concurrent.Future
import com.grasswire.common.json_models._
import play.api.libs.json.Json


/**
 * This controller contains Actions which can be called directly from JavaScript,
 * e.g. ``jsRoutes.controllers.AjaxController.vote(contentId: String, vote: Int);``
 */

@SwaggerApi(value = "/ajax", description = "Ajax API operations")
object AjaxController extends Controller {

  import api.Api.Implicits._
  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def linkPreview(url: String) = Action.async { implicit req =>
    Api.linkPreview(url).get().map(response => new Status(response.status)(response.body))
  }

  def lockdown(locked: Boolean) = Action.async { implicit req =>
    signRequest(Api.lockdown(locked))(r => r.post(Results.EmptyContent()).map(_.pipeStatusBody))
  }

  def searchStories(query: String) = Action.async { implicit req =>
    Api.searchStories(query).get().map { r => new Status(r.status)(r.body) }
  }

  def lockdownStatus = Action.async { implicit req =>
    signRequest(Api.lockdownStatus)(r => r.get().map(_.pipeStatusBody))
  }

  def searchUsers(query: String) = Action.async { implicit req =>
    Api.searchUsers(query).get().map { r => new Status(r.status)(r.body) }
  }

  def listDigests(offset: Int, limit: Int) = Action.async { implicit req =>
    Api.listDigests(offset, limit).get().map(r => new Status(r.status)(r.body))
  }

  def getProfile(username: String) = Action.async { implicit req =>
    Api.userProfile(username).get().map { r => Ok(r.body) }
  }

  def makeAdmin(username: String) = Action.async { implicit req =>
    signRequest(Api.makeAdmin(username))(r => r.post(Results.EmptyContent()).map(_.pipeStatusBody))
  }

  def listNotifications(unreadOnly: Boolean = true, sinceId: Option[Long] = None) = Action.async { implicit req =>
    Future {
      req.session.toGWSession
    }.flatMap {
      case Some(sesh) =>
        signRequest(Api.listNotifications(sesh.user.twitterScreenName, unreadOnly, sinceId))(r => r.get().map(_.pipeStatusBody))
      case _ => Future {
        Forbidden
      }
    }
  }

  def markNotificationsAsRead(ids: String) = Action.async { implicit req =>
    Api.markNotificationsRead(ids).withAuthHeaders(req.session).fold(l => Future(Forbidden),
      r => r.put(Results.EmptyContent()).map { response => new Status(response.status)(response.body) })
  }

  def createStory = Action.async(BodyParsers.parse.json) { implicit req =>
    signRequest(Api.createStory)(r => r.post(req.body).map(_.pipeStatusBody))
  }

  def editStory(id: Long) = Action.async(BodyParsers.parse.json) { implicit req =>
    signRequest(Api.editStory(id))(r => r.put(req.body).map(_.pipeStatusBody))
  }

  def editStoryOrdering = Action.async(BodyParsers.parse.json) { implicit req =>
    signRequest(WS.url(Api.editStoryOrdering))(r => r.put(req.body).map(_.pipeStatusBody))
  }

  def editLink(storyId: Long, linkId: Long, thumbnail: Option[String], title: Option[String], description: Option[String], hidden: Option[Boolean]) = Action.async { implicit req =>
    signRequest(Api.editLink(storyId, linkId, thumbnail, title, description, hidden))(r => r.put(Results.EmptyContent()).map(_.pipeStatusBody))
  }


  def hideStory(storyId: Long) = Action.async { implicit req =>
    signRequest(Api.editStory(storyId))(r => r.put(Json.toJson(EditStoryJsonModel(None, None, None, None, Some(true)))).map(_.pipeStatusBody))
  }

  def unarchiveStory(storyId: Long) = Action.async { implicit req =>
    signRequest(Api.unarchiveStory(storyId))(r => r.put(Results.EmptyContent()).map(_.pipeStatusBody))
  }


  def todaysContributors = Action.async { implicit req =>
    Api.todaysContributors.get().map(_.pipeStatusBody)
  }

  def createLink(url: String, canonicalUrl: String, thumbnail: Option[String], title: String, description: String, storyId: Long) = Action.async { implicit req =>
    implicit val gwSession = req.session.toGWSession
    signRequest(Api.createLink(url, canonicalUrl, thumbnail, title, description, storyId))(r => r.post(Results.EmptyContent()).map(_.pipeStatusBody))
  }

  def updateEmail(email: String) = Action.async { implicit req =>
    req.session.toGWSession match {
      case None => Future {
        Forbidden
      }
      case Some(session) => WS.url(Api.updateEmail).withQueryString("email" -> email, "username" -> session.user.twitterScreenName)
        .withAuthHeaders(req.session).fold(l => Future {
        Forbidden
      }, r => {
        r.put(Results.EmptyContent()).map { r => new Status(r.status)(r.body) }
      })
    }
  }

  def logout = Action.async {
    Future(Ok.withNewSession)
  }
}
