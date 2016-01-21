package com.grasswire.common.apis

import com.grasswire.common.CommonConfig.TwitterConfig
import com.grasswire.common.logging.Logging
import com.grasswire.common.models.{ JsonHelper, Tweet }
import play.api.libs.oauth.{ ConsumerKey, OAuthCalculator, RequestToken }
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import play.api.libs.ws.{ WSResponse, DefaultWSClientConfig, WS }
import play.api.mvc.Results
import scala.concurrent.Future

object TwitterApi extends Logging {

  type TweetId = String
  val `statuses/show`: String = "https://api.twitter.com/1.1/statuses/show.json"
  val clientConfig = new DefaultWSClientConfig()
  val secureDefaults: com.ning.http.client.AsyncHttpClientConfig = new NingAsyncHttpClientConfigBuilder(clientConfig).build()
  val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder(secureDefaults)
  builder.setCompressionEnabled(true)
  val secureDefaultsWithSpecificOptions: com.ning.http.client.AsyncHttpClientConfig = builder.build()
  implicit val implicitClient = new play.api.libs.ws.ning.NingWSClient(secureDefaultsWithSpecificOptions)
  val submissionsOAuthCalc = OAuthCalculator(ConsumerKey(TwitterConfig.Submisions.consumerKey, TwitterConfig.Submisions.consumerSecret), RequestToken(TwitterConfig.Submisions.accessKey, TwitterConfig.Submisions.accessSecret))
  import scala.concurrent.ExecutionContext.Implicits.global

  def show(tweetId: TweetId, oauth: OAuthCalculator): Future[Tweet] = {
    WS.clientUrl(`statuses/show`)
      .withQueryString("id" -> tweetId)
      .sign(oauth)
      .get()
      .map(r => JsonHelper.deserialize[Tweet](r.body))
  }

}
