package com.grasswire.api.http.routing

import akka.actor.ActorSystem
import com.grasswire.api.http._
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.CommonConfig
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.Users
import com.grasswire.common.email.Mailchimp
import com.grasswire.common.json_models.UserProfileJsonModel
import com.grasswire.common.logging.Logging
import com.grasswire.common.models._
import com.grasswire.email.EmailService
import com.grasswire.email.templates.EmailTemplates
import com.wordnik.swagger.annotations._
import play.api.libs.json.{ JsString, JsObject, JsNull }
import play.api.libs.ws.ning.NingWSClient
import spray.http._
import spray.httpx.PlayJsonSupport
import com.sendgrid.SendGrid
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.concurrent.Task

/**
 * Levi Notik
 * Date: 1/12/14
 */

@Api(value = "/users")
class UsersRouter(dal: DAL, redis: scredis.Redis)(implicit ec: ExecutionContext, client: NingWSClient) extends GWDirectives
    with PlayJsonSupport with Logging {

  import spray.routing._
  import MarshallersV1._
  import spray.httpx.marshalling.MetaMarshallers._
  val emailService = EmailService(new SendGrid(CommonConfig.Sendgrid.username, CommonConfig.Sendgrid.password))

  def showUser = path("show") {
    parameter('username) { username =>
      complete {
        Users.findProfile(username)(dal.db) map {
          case Some(u) => (StatusCodes.OK, Some(u): Option[UserProfileJsonModel]) //u.hydrate(dal.db).map(user => (StatusCodes.OK, Some(UserProfileJsonModel(user, Nil)): Option[UserProfileJsonModel]))
          case _ => (StatusCodes.NotFound, None: Option[UserProfileJsonModel])
        }
      }
    }
  }

  def search = path("search") {
    parameter('q.as[String]) { q =>
      get {
        complete {
          Users.search(q)(dal.db)
        }
      }
    }
  }

  def updateEmail = path("update_email") {
    parameters('email, 'username) { (email, username) =>
      requireUserAuth(username)(dal, redis, ec) { _ =>
        put {
          complete {
            Task {
              Users.updateEmail(username, email)(dal.db)
              Task(emailService.send(emailService.create(EmailTemplates.welcomeEmail(email)))).flatMap(_ =>
                Mailchimp.subscribeToList(CommonConfig.mailchimpList, email)).runAsync(_.fold(l => logger.info("error sending email", l), r => ()))
            }.map(_ => JsObject(List("my_name_is" -> JsString("slim shady"))))
          }
        }

      }
    }
  }

  val route: Route = {
    pathPrefix("v1") {
      pathPrefix("users") {
        showUser ~ search ~ updateEmail
      }
    }
  }
}
