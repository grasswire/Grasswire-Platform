package com.grasswire.api.http.routing

import javax.ws.rs.Path

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.GWDirectives
import com.grasswire.common.CommonConfig
import com.grasswire.common.apis.TwitterApi
import com.grasswire.common.config.GWEnvironment
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables._
import com.grasswire.common.json_models._
import com.grasswire.common.logging.Logging
import com.grasswire.common.models._
import com.wordnik.swagger.annotations._
import net.gpedro.integrations.slack.SlackApi
import play.api.libs.json.{ JsObject, JsString }
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.Route

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }
import scala.concurrent.duration._

import com.grasswire.common.Datastore

@Api(value = "/stories", description = "get and create stories")
class StoriesRouter(dal: DAL, redis: scredis.Redis, slackApi: SlackApi)(implicit system: ActorSystem)
    extends GWDirectives with PlayJsonSupport with Logging {

  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext
  val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
  implicit val ec = ExecutionContext.fromExecutorService(executorService)
  logger.info("using stealing pool")

  import com.grasswire.api.http.MarshallersV1._
  import com.grasswire.common.nowUtcMillis

  val gwenv = GWEnvironment(redis, dal)
  val oauth = OAuthCalculator(ConsumerKey(CommonConfig.TwitterConfig.Submisions.consumerKey,
    CommonConfig.TwitterConfig.Submisions.consumerSecret),
    RequestToken(CommonConfig.TwitterConfig.Submisions.accessKey,
      CommonConfig.TwitterConfig.Submisions.accessSecret))
  val route: Route = {
    logRequestResponse(methodUriStatusAsInfoLevel _) {
      compressResponseIfRequested() {
        pathPrefix("v1") {
          pathPrefix("stories") {
            createStory ~ editStory ~ getActiveStories ~ search ~ editStoryOrdering ~ todaysContributors ~ lookupStory ~ setIosOnboarding ~ getIosOnboarding ~ revert
          }
        }
      }
    }
  }

  @ApiOperation(value = "Submit a story", nickname = "createStory", httpMethod = "POST", produces = "application/json", response = classOf[StoryJsonModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "story", value = "the new story to create", required = true, dataType = "CreateStoryJsonModel", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Missing authentication headers or required query param(s)"),
    new ApiResponse(code = 201, message = "Story created")))
  def createStory = pathEndOrSingleSlash {
    post {
      authOrReject(dal, redis, ec) { credentials =>
        checkSiteLock(credentials.username)(dal.db)(ec) {
          entity(as[CreateStoryJsonModel]) { jsonModel =>
            complete {

              Users.find(credentials.username)(dal.db).get.hydrate(dal.db).flatMap { user =>
                val now = nowUtcMillis
                Stories.insert(StoryPEntity(jsonModel.name, credentials.username, jsonModel.summary, jsonModel.headline, jsonModel.coverPhoto, now, now)).run(gwenv) map { result =>
                  val storyJsonModel: StoryJsonModel = StoryJsonModel(jsonModel.name, jsonModel.headline, jsonModel
                    .coverPhoto, jsonModel.summary, result._1, now, now, credentials.username, Nil, result._2, user :: Nil, false)
                  SlackNotifications.notify(NotifyStoryCreated(storyJsonModel), credentials.username)(slackApi)
                  (StatusCodes.Created, storyJsonModel)
                }
              }
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Edit a story", nickname = "editStory", httpMethod = "PUT", produces = "application/json", response = classOf[StoryJsonModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "story_id", value = "id of the story being updated", required = true, dataType = "long",
      paramType = "query"),
    new ApiImplicitParam(name = "name", value = "new name of the story", required = false, dataType = "string",
      paramType = "query"),
    new ApiImplicitParam(name = "summary", value = "new summary of the story", required = false, dataType = "string",
      paramType = "query"),
    new ApiImplicitParam(name = "headline", value = "new headline for this story", required = false, dataType = "string",
      paramType = "query"),
    new ApiImplicitParam(name = "cover_photo", value = "new image url to be used as a cover photo", required = false, dataType = "string",
      paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Missing authentication headers or required query param(s)"),
    new ApiResponse(code = 200, message = "Story updated")))
  def editStory = pathEndOrSingleSlash {
    put {
      authOrReject(dal, redis, ec) { credentials =>
        checkSiteLock(credentials.username)(dal.db)(ec) {
          parameters('story_id.as[Long]) { storyId =>
            entity(as[EditStoryJsonModel]) { jsonModel =>
              complete {
                Stories.update(storyId, credentials.username, jsonModel.name, jsonModel.headline, jsonModel.summary, jsonModel.coverPhoto, jsonModel.hidden)
                  .run(gwenv).map { i =>
                    SlackNotifications.notify(NotifyStoryEdited(i), credentials.username)(slackApi)
                    (StatusCodes.OK, JsObject(List(("status", JsString(s"$i properties updated")))))
                  }
              }
            }
          }
        }
      }
    }
  }

  @Path("/search")
  @ApiOperation(value = "Returns a list of Stories matching a specified query.", httpMethod = "GET",
    response = classOf[StoryJsonModel], responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "q", value = "the search query", required = true, dataType = "String", paramType = "query")
  ))
  def search = path("search") {
    parameter('q.as[String]) { q =>
      get {
        complete {
          Stories.search(q).run(dal.db)
        }
      }
    }
  }

  @Path("/ordering")
  @ApiOperation(value = "Edit story ordering", nickname = "editStoryOrdering", httpMethod = "PUT",
    produces = "application/json", response = classOf[StoryJsonModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "list of new orderings", required = true,
      dataType = "com.grasswire.common.json_models.StoryOrderingJsonModel", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Missing authentication headers or required query param(s)"),
    new ApiResponse(code = 200, message = "Story orderings updated"),
    new ApiResponse(code = 409, message = "Client did not supply an ordering for every currently-ongoing story")))
  def editStoryOrdering = path("ordering") {
    put {
      authOrReject(dal, redis, ec) { credentials =>
        entity(as[List[StoryOrderingJsonModel]]) { ordering =>
          complete {
            Stories.updateOrdering(credentials.username, ordering).run(gwenv).map {
              case -\/(stories) => (StatusCodes.Conflict, stories)
              case \/-(stories) => (StatusCodes.OK, stories)
            }
          }
        }
      }
    }
  }

  @Path("/contributors/today")
  @ApiOperation(value = "Get a list of today's contributors", nickname = "getTodaysContributors", httpMethod = "GET",
    produces = "application/json", response = classOf[UserJsonModel], responseContainer = "List")
  def todaysContributors = path("contributors" / "today") {
    get {
      parameter('high_quality.as[Boolean] ?) { hq =>
        complete {
          val highQuality = hq.getOrElse(false)
          val contributors = Links.contributors().run(dal.db)
          if (highQuality) contributors.map(_.map(user => user.copy(profileImageUrl = user.profileImageUrl.replaceAll("_normal", ""))))
          else contributors
        }
      }
    }
  }

  @ApiOperation(value = "Get currently active stories", nickname = "getActiveStories", httpMethod = "GET",
    produces = "application/json", response = classOf[StoryJsonModel], responseContainer = "List")
  def getActiveStories = pathEndOrSingleSlash {
    get {
      cache(routeCache(timeToLive = 5.seconds)) {
        complete {
          Datastore.getActiveStories(Stories.getActive(gwenv))(gwenv)
        }
      }
    }
  }

  def lookupStory = path("lookup") {
    get {
      parameter('id.as[Long]) { storyId =>
        cache(routeCache(timeToLive = 5.seconds)) {
          complete {
            Datastore.getStoryById(storyId, Stories.getStoryById(storyId, gwenv))(gwenv)
          }
        }
      }
    }
  }

  @Path("/ios_onboarding")
  @ApiOperation(value = "Get tweet for use in iOS onboarding", nickname = "getIosOnboarding", httpMethod = "GET",
    produces = "application/json", response = classOf[Tweet])
  def getIosOnboarding = path("ios_onboarding") {
    get {
      complete {
        IosOnboarding.getCurrentOnboardingTweet.run(dal.db)
      }
    }
  }

  @Path("/ios_onboarding")
  @ApiOperation(value = "set iOS onboarding tweet", nickname = "setIosOnboarding", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "tweet_id", value = "id of tweet", required = true, dataType = "long", paramType = "query")
  ))
  def setIosOnboarding = path("ios_onboarding") {
    post {
      parameter('tweet_id) { tweetId =>
        complete {
          import com.grasswire.common.Implicits.TaskPimps
          Task.fromScalaDeferred {
            TwitterApi.show(tweetId, oauth)
          }.flatMap(t => IosOnboarding.insert(t).run(dal.db))
        }
      }
    }
  }

  def revert = path("revert") {
    post {
      parameters('storyId.as[Long], 'version.as[Long]) { (storyId, version) =>
        requireAdmin(dal, redis, ec) { creds =>
          entity(as[StoryReversionJsonModel]) { reversion =>
            complete {
              StoryChangelogs.revert(version, storyId, reversion, creds.username)(gwenv)
                .map(edits => SlackNotifications.notify(NotifyStoryEdited(edits), creds.username)(slackApi))
                .map(_ => (StatusCodes.OK, JsObject(Seq("status" -> JsString("story reverted")))))
            }
          }
        }
      }
    }
  }
}
