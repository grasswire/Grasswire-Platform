package com.grasswire.common.models

import java.text.SimpleDateFormat

import com.grasswire.common.json_models.VideoLinkJsonData
import com.grasswire.common.parsers._
import org.joda.time.{ DateTimeZone, DateTime }
import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

/**
 * Levi Notik
 * Date: 2/11/14
 */

case class TweetVideoData(videoLinkType: VideoLinkType, embedUrl: String)

case class Tweet(created_at: String,
    entities: Entities,
    favorite_count: Option[Int],
    favorited: Option[Boolean],
    filter_level: Option[String],
    id: Long,
    id_str: String,
    in_reply_to_screen_name: Option[String],
    in_reply_to_status_id: Option[Long],
    in_reply_to_status_id_str: Option[String],
    in_reply_to_user_id: Option[Long],
    in_reply_to_user_id_str: Option[String],
    lang: Option[String],
    possibly_sensitive: Option[Boolean],
    retweet_count: Int,
    retweeted: Boolean,
    retweeted_status: Option[Tweet],
    source: String,
    text: String,
    truncated: Boolean,
    user: TwitterUser) {

  def tweetUrl = s"https://twitter.com/${user.screen_name}/status/$id_str"

  def hasPhoto = entities.media.headOption.exists(_.`type` == "photo")

  sealed trait TweetMediaType

  case object YoutubeVideo extends TweetMediaType

  case object Photo extends TweetMediaType

  val youTubeIdRegex = """https?://(?:[0-9a-zA-Z-]+\.)?(?:youtu\.be/|youtube\.com\S*[^\w\-\s])([\w \-]{11})(?=[^\w\-]|$)(?![?=&+%\w]*(?:[\'"][^<>]*>|</a>))[?=&+%\w-]*""".r

  def hasYoutubeVideo = entities.urls.map(_.expanded_url).exists(youTubeIdRegex.findFirstIn(_).isDefined)

  def getYoutubeEmbedUrl = "https://www.youtube.com/embed/" + youTubeIdRegex.findFirstMatchIn(entities.urls.map(_.expanded_url).find(youTubeIdRegex.findFirstMatchIn(_).isDefined).get).map(_.group(1)).get

  def getYoutubeUrl = "https://www.youtube.com/watch?v=" + youTubeIdRegex.findFirstMatchIn(entities.urls.map(_.expanded_url).find(youTubeIdRegex.findFirstMatchIn(_).isDefined).get).map(_.group(1)).get

  def mediaUrlHttps: Option[String] = entities.media.headOption.map(_.media_url_https)

  def getMediaUrls = entities.media.map(_.media_url_https)

  def mediaUrl: Option[String] = entities.media.headOption.map(_.media_url)

  def getCreateDate: DateTime = {
    val sf = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy")
    sf.setLenient(true)
    val dt = sf.parse(created_at)
    new DateTime(dt).withZone(DateTimeZone.UTC)
  }

  def videoData: List[TweetVideoData] =
    entities.urls.map(_.expanded_url).map(LinkTypeParsers.linktype).collect {
      case vine: VineVideoLinkType => TweetVideoData(vine, s"https://vine.co/v/${vine.videoId}/embed/simple")
      case youtube: YoutubeVideoLinkType => TweetVideoData(youtube, s"https://www.youtube.com/embed/${youtube.videoId}")
    }
}

object Tweet {

  implicit val TweetReads: Reads[Tweet] = (
    (JsPath \ "created_at").read[String] and
    (JsPath \ "entities").read[Entities] and
    (JsPath \ "favorite_count").readNullable[Int] and
    (JsPath \ "favorited").readNullable[Boolean] and
    (JsPath \ "filter_level").readNullable[String] and
    (JsPath \ "id").read[Long] and
    (JsPath \ "id_str").read[String] and
    (JsPath \ "in_reply_to_screen_name").readNullable[String] and
    (JsPath \ "in_reply_to_status_id").readNullable[Long] and
    (JsPath \ "in_reply_to_status_id_str").readNullable[String] and
    (JsPath \ "in_reply_to_user_id").readNullable[Long] and
    (JsPath \ "in_reply_to_user_id_str").readNullable[String] and
    (JsPath \ "lang").readNullable[String] and
    (JsPath \ "possibly_sensitive").readNullable[Boolean] and
    (JsPath \ "retweet_count").read[Int] and
    (JsPath \ "retweeted").read[Boolean] and
    (JsPath \ "retweeted_status").readNullable[Tweet] and
    (JsPath \ "source").read[String] and
    (JsPath \ "text").read[String] and
    (JsPath \ "truncated").read[Boolean] and
    (JsPath \ "user").read[TwitterUser]

  )(Tweet.apply _)

  implicit val TweetWrites: Writes[Tweet] = (
    (JsPath \ "created_at").write[String] and
    (JsPath \ "entities").write[Entities] and
    (JsPath \ "favorite_count").write[Option[Int]] and
    (JsPath \ "favorited").write[Option[Boolean]] and
    (JsPath \ "filter_level").write[Option[String]] and
    (JsPath \ "id").write[Long] and
    (JsPath \ "id_str").write[String] and
    (JsPath \ "in_reply_to_screen_name").write[Option[String]] and
    (JsPath \ "in_reply_to_status_id").write[Option[Long]] and
    (JsPath \ "in_reply_to_status_id_str").write[Option[String]] and
    (JsPath \ "in_reply_to_user_id").write[Option[Long]] and
    (JsPath \ "in_reply_to_user_id_str").write[Option[String]] and
    (JsPath \ "lang").write[Option[String]] and
    (JsPath \ "possibly_sensitive").write[Option[Boolean]] and
    (JsPath \ "retweet_count").write[Int] and
    (JsPath \ "retweeted").write[Boolean] and
    (JsPath \ "retweeted_status").write[Option[Tweet]] and
    (JsPath \ "source").write[String] and
    (JsPath \ "text").write[String] and
    (JsPath \ "truncated").write[Boolean] and
    (JsPath \ "user").write[TwitterUser]

  )(unlift(Tweet.unapply))
}
