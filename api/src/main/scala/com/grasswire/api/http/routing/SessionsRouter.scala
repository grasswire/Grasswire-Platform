package com.grasswire.api.http.routing

import javax.ws.rs.Path

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.CommonConfig
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.{ UserPersistEntity, Users }
import com.grasswire.common.email.Mailchimp
import com.grasswire.common.json_models.UserJsonModel
import com.grasswire.common.logging.Logging
import com.grasswire.common.models._
import com.grasswire.users.PostgresUserManager
import com.wordnik.swagger.annotations.{ Api, ApiImplicitParam, ApiImplicitParams, ApiOperation }
import org.slf4j.LoggerFactory
import play.api.libs.json.{ JsSuccess, Json }
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import play.api.libs.ws.ning.{ NingWSClient, NingAsyncHttpClientConfigBuilder }
import play.api.libs.ws.{ DefaultWSClientConfig, WS }
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing._
import scala.concurrent.Await
import scala.language.postfixOps

@Api(value = "/sessions")
class SessionsRouter(dal: DAL, redis: scredis.Redis)(implicit system: ActorSystem, client: NingWSClient) extends GWDirectives with Json4sSupport with Logging {

  val json4sFormats = JsonHelper.formats

  import scala.concurrent.duration._

  implicit val EC = system.dispatcher

  val pgManager = new PostgresUserManager(dal)

  val myLogger = LoggerFactory.getLogger(getClass.getSimpleName)

  @Path("/twitter")
  @ApiOperation(value = "Login or signup via twitter", httpMethod = "POST", response = classOf[Session])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "token", value = "token from twitter auth API", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "secret", value = "secret from twitter auth API", required = true, dataType = "int", paramType = "query"),
    new ApiImplicitParam(name = "email", value = "new user email if provided", required = false, dataType = "string", paramType = "query")
  ))
  def createTwitterSession = post {
    parameters('token.as[String], 'secret.as[String], 'email.as[String].?) { (token, secret, email) =>
      complete {
        val twitterErrorMsg = "An error occurred while attempting to validate Twitter credentials"
        val requestToken = RequestToken(token, secret)
        val KEY = ConsumerKey(CommonConfig.TwitterConfig.Auth.consumerKey, CommonConfig.TwitterConfig.Auth.consumerSecret)
        val response = Await.result(WS.clientUrl("https://api.twitter.com/1.1/account/verify_credentials.json?skip_status=true").sign(OAuthCalculator(KEY, requestToken)).get(), 5 seconds)
        if (response.status == StatusCodes.Unauthorized.intValue) {
          (StatusCodes.Unauthorized, twitterErrorMsg)
        } else {
          Json.parse(response.body).validate[TwitterUser] match {
            case JsSuccess(twitterUser, _) =>
              val maybeUser = Users.findUser(twitterUser.screen_name)(dal.db)
              maybeUser match {
                case Some(user) =>
                  Users.updateProfileImageUrl(twitterUser)(dal.db)
                  Users.updateTwitterName(twitterUser)(dal.db)

                  (StatusCodes.OK, Session(pgManager.getOrCreateSecret(twitterUser.screen_name)(dal.db).token, UserJsonModel.fromPersistEntity(user)(dal.db), isFirstLogin = false))
                case None =>
                  val newUser = Users.createNewUser(UserPersistEntity.newUser(twitterUser, email))(dal.db)
                  (StatusCodes.Created, Session(pgManager.getOrCreateSecret(twitterUser.screen_name)(dal.db).token, UserJsonModel.fromPersistEntity(newUser)(dal.db), isFirstLogin = true))
              }
            case _ => (StatusCodes.InternalServerError, twitterErrorMsg)
          }
        }
      }
    }
  }

  val route: Route = {
    pathPrefix("v1") {
      pathPrefix("sessions") {
        path("twitter") {
          createTwitterSession
        }
      }
    }
  }

}

