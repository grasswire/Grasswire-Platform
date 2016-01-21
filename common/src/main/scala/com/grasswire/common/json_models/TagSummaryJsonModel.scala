package com.grasswire.common.json_models

import com.grasswire.common.Username
import play.api.libs.json.Writes

case class TagSummaryJsonModel(author: Username, summary: String, version: Long, createdAt: Long)

object TagSummaryJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val jsonReads: Reads[TagSummaryJsonModel] = (
    (JsPath \ "author").read[String] and
    (JsPath \ "summary").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "createdAt").read[Long]
  )(TagSummaryJsonModel.apply _)

  implicit val writes: Writes[TagSummaryJsonModel] = (
    (JsPath \ "author").write[String] and
    (JsPath \ "summary").write[String] and
    (JsPath \ "version").write[Long] and
    (JsPath \ "createdAt").write[Long]
  )(unlift(TagSummaryJsonModel.unapply))
}

case class TagHeadlineJsonModel(author: Username, headline: String, version: Long, createdAt: Long)

object TagHeadlineJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val jsonReads: Reads[TagHeadlineJsonModel] = (
    (JsPath \ "author").read[String] and
    (JsPath \ "headline").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "createdAt").read[Long]
  )(TagHeadlineJsonModel.apply _)

  implicit val writes: Writes[TagHeadlineJsonModel] = (
    (JsPath \ "author").write[String] and
    (JsPath \ "headline").write[String] and
    (JsPath \ "version").write[Long] and
    (JsPath \ "createdAt").write[Long]
  )(unlift(TagHeadlineJsonModel.unapply))
}

case class TagPhotoJsonModel(author: Username, photoUrl: String, version: Long, createdAt: Long)

object TagPhotoJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val jsonReads: Reads[TagPhotoJsonModel] = (
    (JsPath \ "author").read[String] and
    (JsPath \ "photoUrl").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "createdAt").read[Long]
  )(TagPhotoJsonModel.apply _)

  implicit val writes: Writes[TagPhotoJsonModel] = (
    (JsPath \ "author").write[String] and
    (JsPath \ "photoUrl").write[String] and
    (JsPath \ "version").write[Long] and
    (JsPath \ "createdAt").write[Long]
  )(unlift(TagPhotoJsonModel.unapply))
}