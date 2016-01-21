package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Media(id: Long,
  id_str: String,
  indices: Seq[Int],
  media_url: String,
  media_url_https: String,
  url: String,
  display_url: String,
  expanded_url: String,
  `type`: String, sizes: Sizes)

object Media {
  implicit val MediaReads: Reads[Media] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "id_str").read[String] and
    (JsPath \ "indices").read[Seq[Int]] and
    (JsPath \ "media_url").read[String] and
    (JsPath \ "media_url_https").read[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "display_url").read[String] and
    (JsPath \ "expanded_url").read[String] and
    (JsPath \ "type").read[String] and
    (JsPath \ "sizes").read[Sizes]
  )(Media.apply _)

  implicit val MediaWrites: Writes[Media] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "id_str").write[String] and
    (JsPath \ "indices").write[Seq[Int]] and
    (JsPath \ "media_url").write[String] and
    (JsPath \ "media_url_https").write[String] and
    (JsPath \ "url").write[String] and
    (JsPath \ "display_url").write[String] and
    (JsPath \ "expanded_url").write[String] and
    (JsPath \ "type").write[String] and
    (JsPath \ "sizes").write[Sizes]
  )(unlift(Media.unapply))
}

