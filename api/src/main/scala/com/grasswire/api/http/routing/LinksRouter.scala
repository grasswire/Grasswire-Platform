package com.grasswire.api.http.routing

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.api.http.security.Security.GWAuthCredentials
import com.grasswire.common._
import com.grasswire.common.apis.TwitterApi
import com.grasswire.common.config.GWEnvironment
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.{ Users, Links }
import com.grasswire.common.json_models._
import com.grasswire.common.models.JsonHelper
import com.grasswire.common.parsers._
import com.rabbitmq.client.Channel
import com.wordnik.swagger.annotations._
import javax.ws.rs.Path
import net.gpedro.integrations.slack.SlackApi
import org.json4s.JsonAST.{ JObject, JString, JNull }
import play.api.libs.json.{ Json, JsString, JsObject, JsNull }
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import spray.http.{ StatusCode, StatusCodes }
import spray.httpx.marshalling.ToResponseMarshallable
import spray.httpx.{ PlayJsonSupport, Json4sSupport }
import spray.routing.Route

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scalaz._
import scalaz.concurrent.Task

/**
 * Created by levinotik on 3/16/15.
 */
@Api(value = "/links", description = "get and submit links")
class LinksRouter(dal: DAL, redis: scredis.Redis, slackApi: SlackApi)(implicit system: ActorSystem, ec: ExecutionContext)
    extends GWDirectives with PlayJsonSupport {

  val gwenv = GWEnvironment(redis, dal)

  import LinksRouter._
  import com.grasswire.api.http.MarshallersV1._

  val route: Route = {
    logRequestResponse(methodUriStatusAsInfoLevel _) {
      compressResponseIfRequested() {
        pathPrefix("v1") {
          pathPrefix("links") {
            pathEndOrSingleSlash {
              createLink ~ editLink
            } ~ path("show") {
              getLinkById
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Submit a new link", nickname = "createLink", httpMethod = "POST", produces = "application/json", response = classOf[LinkJsonModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "story_id", value = "id of story this link belongs to", required = true, dataType = "long", paramType = "query"),
    new ApiImplicitParam(name = "url", value = "the url to submit", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "thumbnail", value = "url of a thumbnail image preview", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "title", value = "title for this link", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "description", value = "description for this link", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "canonical_url", value = "canonical url for this link", required = true, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Missing authentication headers or required query param(s)"),
    new ApiResponse(code = 201, message = "Link created")))
  def createLink = post {
    parameters('story_id.as[Long], 'url, 'thumbnail ?, 'title, 'description, 'canonical_url) { (storyId, url, thumbnail, title, description, canonicalUrl) =>
      authOrReject(dal, redis, ec) { credentials =>
        checkSiteLock(credentials.username)(dal.db)(ec) {
          complete {
            submitLink(credentials, url, canonicalUrl, storyId, thumbnail, title, description).run(gwenv).map { l =>
              SlackNotifications.notify(NotifyLinkSubmitted(l), credentials.username)(slackApi)
              l
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Edit a link", nickname = "editLink", httpMethod = "PUT", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "link_id", value = "id of link beying updated", required = true, dataType = "long", paramType = "query"),
    new ApiImplicitParam(name = "story_id", value = "new story id this link belongs to", required = false, dataType = "long", paramType = "query"),
    new ApiImplicitParam(name = "thumbnail", value = "new url of a thumbnail image preview", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "title", value = "new title for this link", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "hidden", value = "new hidden status", required = false, dataType = "Boolean", paramType = "query"),
    new ApiImplicitParam(name = "description", value = "new description for this link", required = false, dataType = "string", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Missing authentication headers or required query param(s)"),
    new ApiResponse(code = 201, message = "Link created")))
  def editLink = put {
    parameters('link_id.as[Long], 'story_id.as[Long] ?, 'thumbnail ?, 'title.?, 'description.?, 'hidden.as[Boolean] ?) { (linkId, storyId, thumbnail, title, description, hidden) =>
      authOrReject(dal, redis, ec) { credentials =>
        checkSiteLock(credentials.username)(dal.db)(ec) {
          complete {
            Links.update(linkId, credentials.username, storyId, thumbnail, title, description, hidden).run(gwenv).map {
              case 0 => (StatusCodes.NotFound, JsObject(List(("status", JsString(s"no link with id $linkId found")))))
              case n => (StatusCodes.OK, JsObject(List("status" -> JsString("link updated"))))
            }
          }
        }
      }
    }
  }

  @Path("/show")
  @ApiOperation(value = "Retrieve a link", httpMethod = "GET", response = classOf[LinkJsonModel], produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "id of link submission", required = false, dataType = "long", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Link not found"),
    new ApiResponse(code = 200, message = "Link retrieved successfully")))
  def getLinkById = get {
    parameters('id.as[Long], 'include_story.as[Boolean] ?) { (id, includeStory) =>
      complete {
        if (includeStory.getOrElse(false)) {
          Links.showLink(id).run(GWEnvironment(redis, dal))
        } else {
          Links.findById(id).run(dal.db).map {
            case None => (StatusCodes.NotFound, None: Option[LinkJsonModel])
            case Some(t) => (StatusCodes.OK, Some(t))
          }
        }
      }
    }
  }
}

object LinksRouter {

  val oauth = OAuthCalculator(ConsumerKey(CommonConfig.TwitterConfig.Submisions.consumerKey,
    CommonConfig.TwitterConfig.Submisions.consumerSecret),
    RequestToken(CommonConfig.TwitterConfig.Submisions.accessKey,
      CommonConfig.TwitterConfig.Submisions.accessSecret))

  import com.grasswire.common.Implicits.TaskPimps

  def submitLink(creds: GWAuthCredentials, url: String, canonicalUrl: String, storyId: Long, thumbnail: Option[String],
    title: String, description: String)(implicit ec: ExecutionContext): ReaderT[Task, GWEnvironment, LinkJsonModel] =
    Kleisli[Task, GWEnvironment, LinkJsonModel] { env =>
      val now: Long = com.grasswire.common.nowUtcMillis
      LinkTypeParsers.linktype(url) match {
        case TweetLinkType(id) =>
          for {
            user <- Task(Users.findUser(creds.username)(env.db).get).flatMap(u => u.hydrate(env.db))
            tweet <- Task.fromScalaDeferred(TwitterApi.show(id, oauth))
            result <- Links.insertTweet(tweet, creds.username, storyId).run(env)
              .map(insertId => TweetLinkJsonModel(tweet, creds.username, storyId, now, hidden = false, insertId, user))
          } yield result

        case video: VideoLinkType =>
          val videoLinkData = VideoLinkJsonData.makeData(url, thumbnail, title, description, canonicalUrl, video)
          for {
            user <- Task(Users.findUser(creds.username)(env.db).get).flatMap(u => u.hydrate(env.db))
            insertId <- Links.insertVideo(videoLinkData, creds.username, storyId, video).run(env)
          } yield VideoLinkJsonModel(videoLinkData, creds.username, storyId, now, hidden = false, insertId, user)

        case PlainLinkType =>
          val linkData = PlainLinkJsonData(thumbnail, url, canonicalUrl, description, title)
          for {
            user <- Task(Users.findUser(creds.username)(env.db).get).flatMap(u => u.hydrate(env.db))
            insertId <- Links.insertPlainLink(linkData, creds.username, storyId).run(env)
          } yield PlainLinkJsonModel(linkData, creds.username, storyId, now, hidden = false, insertId, user)

        case _ => Task.fail(new Exception("unsupported link type"))
      }
    }

}
