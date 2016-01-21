package com.grasswire.common.json_models

import com.grasswire.common._

case class TitleJsonModel(author: String,
  title: String,
  contentId: java.util.UUID,
  createDate: Long,
  id: Long)

object TitleJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[TitleJsonModel] = (
    (JsPath \ "author").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "contentId").read[java.util.UUID] and
    (JsPath \ "createDate").read[Long] and
    (JsPath \ "id").read[Long]
  )(TitleJsonModel.apply _)
}
