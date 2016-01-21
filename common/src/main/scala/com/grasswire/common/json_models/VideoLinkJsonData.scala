package com.grasswire.common.json_models

import com.grasswire.common.parsers.{ YoutubeVideoLinkType, VineVideoLinkType, VideoLinkType }
import play.api.libs.json.Writes

/**
 * Created by levinotik on 6/23/15.
 */
case class VideoLinkJsonData(url: String,
  thumbnail: Option[String],
  provider: String,
  embed_url: String,
  canonicalUrl: String,
  title: String,
  description: String)

object VideoLinkJsonData {
  def makeData(url: String, thumbnail: Option[String], title: String, description: String, canonicalUrl: String, videoLinkType: VideoLinkType) = videoLinkType match {
    case vine: VineVideoLinkType => VideoLinkJsonData(url, thumbnail, "vine", s"https://vine.co/v/${vine.videoId}/embed/simple", canonicalUrl, title, description)
    case youtube: YoutubeVideoLinkType => VideoLinkJsonData(url, thumbnail, "youtube", s"https://www.youtube.com/embed/${youtube.videoId}", canonicalUrl, title, description)
  }

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[VideoLinkJsonData] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "thumbnail").readNullable[String] and
    (JsPath \ "provider").read[String] and
    (JsPath \ "embed_url").read[String] and
    (JsPath \ "canonicalUrl").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "description").read[String]
  )(VideoLinkJsonData.apply _)

  implicit val writes: Writes[VideoLinkJsonData] = (
    (JsPath \ "url").write[String] and
    (JsPath \ "thumbnail").writeNullable[String] and
    (JsPath \ "provider").write[String] and
    (JsPath \ "embed_url").write[String] and
    (JsPath \ "canonicalUrl").write[String] and
    (JsPath \ "title").write[String] and
    (JsPath \ "description").write[String]
  )(unlift(VideoLinkJsonData.unapply))

}
