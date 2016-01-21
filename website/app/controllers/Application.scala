package controllers


import api.Api
import com.grasswire.common.json_models._
import play.api._
import play.api.libs.json.{JsSuccess, Json, JsError}
import play.api.libs.ws.WSRequestHolder
import play.api.mvc._
import play.api.Routes._
import security.Secured
import services.RenderHtml
import services.SessionService.SessionOps

import scala.concurrent.Future
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller with Secured {


  import play.api.libs.ws.WS

  def googleVerify = Action.async { _ =>
    Future {
      Ok(views.html.google.google_verify())
    }
  }

  def legacyCatchAllRedirect(x: String) = Action.async(implicit req =>
    Future {
      implicit val gwSession = req.session.toGWSession

      Redirect(routes.Application.index(None))
    }

  )

  def guidelines =
    Action.async(implicit req =>
      Future {
        implicit val gwSession = req.session.toGWSession

        Ok(views.html.pages.static.guidelines())
      }
    )

  def live = Action.async { implicit req =>
    implicit val gwSession = req.session.toGWSession
      Api.getLivePage.get().map(r => Json.parse(r.body).validate[LivePageJsonModel].get)
      .map(livepage => Ok(views.html.pages.grasswire_live(livepage)))
  }

  def about =
    Action.async(implicit req =>
      Future {
        implicit val gwSession = req.session.toGWSession

        Ok(views.html.pages.static.about())
      }
    )

  def index(modal: Option[String] = None) = Action.async { implicit request =>
    implicit val gwSession = request.session.toGWSession
    for {
      stories <- WS.url(Api.getActiveStories).get().map { r =>
        r.json.validate[List[StoryJsonModel]]
      }
    } yield {
      stories match {
        case JsSuccess(s, _) => Ok(views.html.pages.reader(s))
        case JsError(errors) =>
        Logger.info(errors.mkString("\n"))
        Ok(views.html.pages.static.error_500())
      }
    }
  }

  def feed(modal: Option[String] = None) = Action.async { implicit request =>
    implicit val gwSession = request.session.toGWSession
    for {
      stories <- WS.url(Api.getActiveStories).get().map { r =>
        r.json.validate[List[StoryJsonModel]]
      }
    } yield {
      stories match {
        case JsSuccess(s, _) => Ok(views.html.feed(s)).as("text/xml")
        case _ => Ok(views.html.pages.static.error_500())
      }
    }
  }

  def lookupStory(storyId: Long, name: String) = Action.async { implicit request =>
    implicit val gwSession = request.session.toGWSession
    Api.lookupStory(storyId).get().map { r =>
      if (r.status == OK) {
        r.json.validate[StoryJsonModel] match {
          case JsSuccess(s, _) => Ok(views.html.pages.reader(List(s), storyId = Some(s.id)))
          case _ => Ok(views.html.pages.static.error_500())
        }
      } else {
        Ok(views.html.pages.static.error_404())
      }
    }
  }

  def showLink(linkId: Long) = Action.async { implicit request =>
    implicit val gwSession = request.session.toGWSession
    Api.showLink(linkId).get().map { r =>
      if (r.status == OK) {
        r.json.validate[ShowLinkJsonModel] match {
          case JsSuccess(s, _) => Ok(s.toString)
          case _ => Ok(views.html.pages.static.error_500())
        }
      } else {
        Ok(views.html.pages.static.error_404())
      }
    }

  }


  def edit(modal: Option[String] = None, storyId: Option[Long] = None) = withAuth { _ => implicit request =>
    implicit val gwSession = request.session.toGWSession

    storyId match {
      case Some(id) =>
        Api.lookupStory(id).get().map { r =>
          if (r.status == OK) {
            r.json.validate[StoryJsonModel] match {
              case JsSuccess(s, _) => Ok(views.html.pages.editor(List(s)))
              case _ => Ok(views.html.pages.static.error_500())
            }
          } else {
            Ok(views.html.pages.static.error_404())
          }
        }

      case _ =>
        for {
          stories <- WS.url(Api.getActiveStories).get().map { r =>
            r.json.validate[List[StoryJsonModel]].get
          }
        } yield Ok(views.html.pages.editor(stories))
    }


  }

  def newStory(modal: Option[String] = None) =
    Action.async(implicit req =>
      Future {
        implicit val gwSession = req.session.toGWSession

        Ok(views.html.pages.new_story())
      }
    )


  def login = {
    Action.async { implicit req =>
      Future {
        Redirect(routes.Application.index(None))
      }

    }
  }


  def logout = Action.async { implicit req =>
      Future {
        Redirect(routes.Application.index(modal = None)).withNewSession
      }

    }


  def healthCheck = Action.async {
    Future {
      Ok(views.html.pages.healthcheck())
    }
  }

  def userProfile(username: String) = Action.async { implicit req =>
    implicit val session = req.session.toGWSession

    Api.userProfile(username).get().map { response =>

      if (response.status == 404) {
        Ok(views.html.pages.static.error_404())
      } else {
        Json.parse(response.body).validate[UserProfileJsonModel].fold(l => Ok(views.html.pages.static.error_500()),
          r => {
            val modifiedProfileImage = r.user.profileImageUrl.replaceAll("_normal", "")
            Ok(views.html.pages.user_profile(r.copy(user = r.user.copy(profileImageUrl = modifiedProfileImage))))
          })
      }

    }

  }

  def javascriptRoutes = Action.async { implicit request =>
    Future {
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.AjaxController.createLink,
          routes.javascript.AjaxController.updateEmail,
          routes.javascript.AjaxController.createStory,
          routes.javascript.AjaxController.editStory,
          routes.javascript.AjaxController.editStoryOrdering,
          routes.javascript.AjaxController.editLink,
          routes.javascript.AjaxController.searchStories,
          routes.javascript.AjaxController.searchUsers,
          routes.javascript.AjaxController.makeAdmin,
          routes.javascript.AjaxController.lockdown,
          routes.javascript.AjaxController.lockdownStatus,
          routes.javascript.AjaxController.unarchiveStory,
          routes.javascript.AjaxController.hideStory,
          routes.javascript.AjaxController.getProfile,
          routes.javascript.AjaxController.listNotifications,
          routes.javascript.AjaxController.markNotificationsAsRead,
          routes.javascript.Application.logout,
          routes.javascript.Application.login,
          routes.javascript.Application.index,
          routes.javascript.AdminController.createDigest,
          routes.javascript.AdminController.generateEmailDigest,
          routes.javascript.AdminController.revertStory,
          routes.javascript.AdminController.hellban,
          routes.javascript.AdminController.createLivePage,
          routes.javascript.AjaxController.linkPreview,
          routes.javascript.AjaxController.todaysContributors
        )
      ).as("text/javascript")
    }
  }

}
