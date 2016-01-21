package com.grasswire.common.json_models

import play.api.libs.json.Writes

case class PhotoLinkJsonData(provider: String, embedUrl: Option[String])

object PhotoLinkJsonData {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[PhotoLinkJsonData] = (
    (JsPath \ "provider").read[String] and
    (JsPath \ "embedUrl").readNullable[String]
  )(PhotoLinkJsonData.apply _)

  implicit val writes: Writes[PhotoLinkJsonData] = (
    (JsPath \ "provider").write[String] and
    (JsPath \ "embedUrl").writeNullable[String]
  )(unlift(PhotoLinkJsonData.unapply))
}
