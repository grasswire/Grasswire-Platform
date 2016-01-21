package com.grasswire.common.models.http

case class LivePagePostModel(title: String, videoEmbedUrl: String)

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._
object LivePagePostModel {

  implicit val reader: Reads[LivePagePostModel] = (
    (JsPath \ "title").read[String] and
    (JsPath \ "videoEmbedUrl").read[String]
  )(LivePagePostModel.apply _)
}
