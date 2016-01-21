package com.grasswire.api.http.routing

import javax.ws.rs.Path

import akka.actor.ActorSystem
import com.grasswire.api.http.directives.{ CORSSupport, GWDirectives }
import com.grasswire.common.db.DAL
import com.grasswire.common.db.tables.Digests
import com.grasswire.common.json_models.{ DigestJsonModel, StoryJsonModel }
import com.grasswire.common.logging.Logging
import com.wordnik.swagger.annotations._
import spray.http.{ StatusCode, StatusCodes }
import spray.httpx.PlayJsonSupport
import spray.routing.Route

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task

@Api(value = "/digests")
class DigestsRouter(dal: DAL, redis: scredis.Redis)(implicit system: ActorSystem, ec: ExecutionContext) extends GWDirectives
    with PlayJsonSupport with CORSSupport with Logging {

  import com.grasswire.api.http.MarshallersV1._

  val route: Route =
    logRequestResponse(methodUriStatusAsInfoLevel _) {
      compressResponseIfRequested() {
        pathPrefix("v1") {
          pathPrefix("digests") {
            createDigest ~ updateDigest ~ getCurrentDigest ~ getDigest ~ listDigests
          }
        }
      }
    }

  def createDigest = pathEndOrSingleSlash {
    post {
      requireAdmin(dal, redis, ec) { _ =>
        entity(as[List[StoryJsonModel]]) { digest =>
          complete {
            Digests.insert(digest).run(dal.db)
          }
        }
      }
    }
  }

  def updateDigest = pathEndOrSingleSlash {
    put {
      requireAdmin(dal, redis, ec) { _ =>
        parameter('id.as[Long]) { id =>
          entity(as[List[StoryJsonModel]]) { topics =>
            complete {
              Digests.update(id, topics).run(dal.db)
            }
          }
        }
      }
    }
  }

  @Path("/current")
  @ApiOperation(value = "Retrieve the most recent digest", httpMethod = "GET", response = classOf[DigestJsonModel])
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Digest not found")))
  def getCurrentDigest = cors {
    path("current") {
      get {
        complete {
          Digests.getCurrent.run(dal.db)
            .map {
              case Some(d) => (StatusCodes.OK, Some(d))
              case None => (StatusCodes.NotFound, None)
            }: Task[(StatusCode, Option[DigestJsonModel])]
        }
      }
    }
  }

  @ApiOperation(value = "Retrieve a digest", httpMethod = "GET", response = classOf[DigestJsonModel])
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Digest not found")))
  def getDigest = pathEndOrSingleSlash {
    get {
      parameter('id.as[Long]) { id =>
        complete {
          Digests.getById(id).run(dal.db)
            .map {
              case Some(d) => (StatusCodes.OK, Some(d))
              case None => (StatusCodes.NotFound, None)
            }: Task[(StatusCode, Option[DigestJsonModel])]
        }
      }
    }
  }

  def listDigests = path("list") {
    parameters('offset.as[Int], 'limit.as[Int]) { (offset, limit) =>
      complete {
        Digests.list(offset, limit).run(dal.db)
      }
    }
  }
}
