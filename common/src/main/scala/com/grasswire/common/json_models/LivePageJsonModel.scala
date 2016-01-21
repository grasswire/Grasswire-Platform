package com.grasswire.common.json_models

import com.grasswire.common._
import play.api.libs.json.{ Reads, Writes }

case class LivePageJsonModel(title: String, videoEmbedUrl: String, createdBy: Username, createdAt: Long, id: Long)

import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._
object LivePageJsonModel {

  implicit val writer: Writes[LivePageJsonModel] = (
    (JsPath \ "title").write[String] and
    (JsPath \ "videoEmbedUrl").write[String] and
    (JsPath \ "createdBy").write[String] and
    (JsPath \ "createdAt").write[Long] and
    (JsPath \ "id").write[Long]
  )(unlift(LivePageJsonModel.unapply))

  implicit val reader: Reads[LivePageJsonModel] = (
    (JsPath \ "title").read[String] and
    (JsPath \ "videoEmbedUrl").read[String] and
    (JsPath \ "createdBy").read[String] and
    (JsPath \ "createdAt").read[Long] and
    (JsPath \ "id").read[Long]
  )(LivePageJsonModel.apply _)
}
