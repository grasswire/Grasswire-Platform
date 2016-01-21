package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Url(url: String, expanded_url: String, display_url: String, indices: Seq[Int])

object Url {
  implicit val UrlReads: Reads[Url] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "expanded_url").read[String] and
    (JsPath \ "display_url").read[String] and
    (JsPath \ "indices").read[Seq[Int]]
  )(Url.apply _)

  implicit val UrlWrites: Writes[Url] = (
    (JsPath \ "url").write[String] and
    (JsPath \ "expanded_url").write[String] and
    (JsPath \ "display_url").write[String] and
    (JsPath \ "indices").write[Seq[Int]]
  )(unlift(Url.unapply))
}

