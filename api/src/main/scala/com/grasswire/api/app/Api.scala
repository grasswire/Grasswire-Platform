package com.grasswire.api.app

import com.gettyimages.spray.swagger._
import com.grasswire.api.config.APIConfig
import com.grasswire.api.http.routing._
import play.api.libs.ws.DefaultWSClientConfig
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import spray.routing.HttpService

import scala.reflect.runtime.universe._

trait Api extends HttpService with CoreActors with Core {

  implicit def executionContext = actorRefFactory.dispatcher

  val clientConfig = new DefaultWSClientConfig()
  val secureDefaults: com.ning.http.client.AsyncHttpClientConfig = new NingAsyncHttpClientConfigBuilder(clientConfig).build()
  val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder(secureDefaults)
  builder.setCompressionEnabled(true)
  val secureDefaultsWithSpecificOptions: com.ning.http.client.AsyncHttpClientConfig = builder.build()
  implicit val implicitClient = new play.api.libs.ws.ning.NingWSClient(secureDefaultsWithSpecificOptions)

  val routes =
    get {
      pathPrefix("swag") {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~
        getFromResourceDirectory("swagger-ui")
    } ~
      new BaseHttpService().route ~
      new UsersRouter(dal, redis).route ~
      new SessionsRouter(dal, redis).route ~
      new AdminRouter(dal, redis).route ~
      new StoriesRouter(dal, redis, slackApi).route ~
      new DigestsRouter(dal, redis).route ~
      new LinksRouter(dal, redis, slackApi).route ~
      new ChangelogsRouter(dal).route ~
      new LivePagesRouter(dal).route ~
      new SwaggerHttpService {
        def actorRefFactory = system
        def apiTypes = Seq(typeOf[LinksRouter], typeOf[DigestsRouter], typeOf[StoriesRouter])
        def apiVersion = "v1"
        def baseUrl = s"${APIConfig.getSwaggerBaseUrl}"
      }.routes
}
