package com.grasswire.common.json_models

import com.grasswire.common._
import play.api.libs.json.Writes

case class StoryChangelogJsonModel(name: String,
  updatedBy: Username,
  summary: Option[String],
  headline: Option[String],
  coverPhoto: Option[String],
  hidden: Boolean,
  storyId: Long,
  id: Long)

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._
object StoryChangelogJsonModel {

  implicit val reads: Reads[StoryChangelogJsonModel] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "updatedBy").read[String] and
    (JsPath \ "summary").readNullable[String] and
    (JsPath \ "headline").readNullable[String] and
    (JsPath \ "coverPhoto").readNullable[String] and
    (JsPath \ "hidden").read[Boolean] and
    (JsPath \ "storyId").read[Long] and
    (JsPath \ "id").read[Long]
  )(StoryChangelogJsonModel.apply _)

  implicit val writes: Writes[StoryChangelogJsonModel] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "updatedBy").write[String] and
    (JsPath \ "summary").writeNullable[String] and
    (JsPath \ "headline").writeNullable[String] and
    (JsPath \ "coverPhoto").writeNullable[String] and
    (JsPath \ "hidden").write[Boolean] and
    (JsPath \ "storyId").write[Long] and
    (JsPath \ "id").write[Long]
  )(unlift(StoryChangelogJsonModel.unapply))
}

case class StoryReversionJsonModel(revertHeadline: Boolean,
  revertSummary: Boolean,
  revertName: Boolean,
  revertHidden: Boolean,
  revertCoverPhoto: Boolean)

object StoryReversionJsonModel {
  implicit val reads: Reads[StoryReversionJsonModel] = (
    (JsPath \ "revertHeadline").read[Boolean] and
    (JsPath \ "revertSummary").read[Boolean] and
    (JsPath \ "revertName").read[Boolean] and
    (JsPath \ "revertHidden").read[Boolean] and
    (JsPath \ "revertCoverPhoto").read[Boolean]
  )(StoryReversionJsonModel.apply _)
}
