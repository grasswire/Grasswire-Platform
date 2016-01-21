package controllers

import api.Api
import com.grasswire.common.json_models._
import com.grasswire.common.models.PagedResult
import play.api.Logger
import play.api.libs.json.{JsString, JsObject, Json}
import play.api.libs.ws.WS
import play.api.mvc.{Results, Action, BodyParsers, Controller}
import security.Secured
import services.SessionService.SessionOps
import Api.signRequest

import scala.concurrent.Future

object AdminController extends Controller with Secured {

  import api.Api.Implicits._
  import play.api.Play.current
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def adminPage = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    Future {
      Ok(views.html.pages.admin.digest())
    }
  }

  def usersPage = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    Future {
      Ok(views.html.pages.admin.users())
    }
  }

  def changeLog(offset: Option[Int], max: Option[Int]) = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    for {
      changeLogs <- Api.changeLogs(offset.getOrElse(0), max.getOrElse(25)).get().map(response => Json.parse(response.body).validate[PagedResult[ChangelogJsonModel]].get)
    } yield Ok(views.html.pages.admin.change_logs(changeLogs))
  }

  def livePage = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    Future {
      Ok(views.html.pages.admin.live())
    }
  }

  def generateEmailDigestPage = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    Future {
      Ok(views.html.pages.admin.generate_email())
    }
  }


  def storyChangeLogs(id: Long, offset: Option[Int], max: Option[Int]) = withAdminAuth { _ => implicit req =>
    implicit val gwSession = req.session.toGWSession
    for {
      changeLogs <- Api.storyChangeLogs(id, offset.getOrElse(0), max.getOrElse(25)).get().map(response => Json.parse(response.body).validate[PagedResult[StoryChangelogJsonModel]].get)
    } yield Ok(views.html.pages.admin.story_change_logs(changeLogs))
  }

  def createDigest = Action.async(BodyParsers.parse.json) { implicit req =>
    implicit val gwSession = req.session.toGWSession
    Logger.info(req.body.toString())
    Api.createDigest.authorize.fold(l => {Logger.error(l.toString()); Future(l.toHttpStatus)}, r => r
      .request
      .withContentTypeJson
      .post(req.body)
      .map(response => response.pipeStatusBody))
  }

  def revertStory(storyId: Long, version: Long) = Action.async(BodyParsers.parse.json) { implicit req =>
    Api.revertStory(storyId, version).authorize.fold(l => Future(l.toHttpStatus), r => r.request
      .withContentTypeJson
      .post(req.body)
      .map(response => response.pipeStatusBody))
  }

  def createLivePage = Action.async(BodyParsers.parse.json) { implicit req =>
    signRequest(Api.createLivePage)(request => request.post(req.body).map(_.pipeStatusBody))
  }

  def hellban(username: String, ban: Boolean) = Action.async { implicit req =>
    signRequest(Api.hellban(username, ban))(r => r.put(Results.EmptyContent()).map(_.pipeStatusBody))
  }

  def generateEmailDigest(limit: Int) = Action.async { implicit req =>
      for {
       stories <-  WS.url(Api.getActiveStories).get().map(response => Json.parse(response.body).validate[List[StoryJsonModel]].get.sortBy(story => story.rank).take(limit))
       storiesWithPosition <- Future.successful(stories.map(story => EmailDigestTemplateModel(story.rank, story)))
      } yield Ok(JsObject(List("email" ->  JsString(views.html.email_digest(storiesWithPosition).body))))
    } 
}
