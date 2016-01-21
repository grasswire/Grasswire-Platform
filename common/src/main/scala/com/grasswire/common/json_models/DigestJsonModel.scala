package com.grasswire.common.json_models

import play.api.libs.json.Writes

case class DigestJsonModel(id: Long, createdAt: Long, stories: List[StoryJsonModel])

object DigestJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[DigestJsonModel] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "createdAt").read[Long] and
    (JsPath \ "stories").read[List[StoryJsonModel]]
  )(DigestJsonModel.apply _)

  implicit val writes: Writes[DigestJsonModel] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "createdAt").write[Long] and
    (JsPath \ "stories").write[List[StoryJsonModel]]
  )(unlift(DigestJsonModel.unapply))
}

