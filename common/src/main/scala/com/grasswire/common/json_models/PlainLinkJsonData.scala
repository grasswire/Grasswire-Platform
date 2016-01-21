package com.grasswire.common.json_models

import play.api.libs.json.Writes

/**
 * Created by levinotik on 6/23/15.
 */
case class PlainLinkJsonData(thumbnail: Option[String],
  url: String,
  canonicalUrl: String,
  description: String,
  title: String)

object PlainLinkJsonData {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[PlainLinkJsonData] = (
    (JsPath \ "thumbnail").readNullable[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "canonicalUrl").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "title").read[String]
  )(PlainLinkJsonData.apply _)

  implicit val writes: Writes[PlainLinkJsonData] = (
    (JsPath \ "thumbnail").writeNullable[String] and
    (JsPath \ "url").write[String] and
    (JsPath \ "canonicalUrl").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "title").write[String]
  )(unlift(PlainLinkJsonData.unapply))

}
