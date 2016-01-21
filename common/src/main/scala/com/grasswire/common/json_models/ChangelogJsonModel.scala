package com.grasswire.common.json_models

import com.grasswire.common._
import org.joda.time.{ DateTimeZone, DateTime }
import play.api.libs.json.Writes

case class ChangelogJsonModel(user: Username, action: String, storyId: Long, linkId: Option[Long], reason: Option[String], datetime: Long, id: Long)

object ChangelogJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[ChangelogJsonModel] = (
    (JsPath \ "user").read[String] and
    (JsPath \ "action").read[String] and
    (JsPath \ "storyId").read[Long] and
    (JsPath \ "linkId").readNullable[Long] and
    (JsPath \ "reason").readNullable[String] and
    (JsPath \ "datetime").read[Long] and
    (JsPath \ "id").read[Long]
  )(ChangelogJsonModel.apply _)

  implicit val writes: Writes[ChangelogJsonModel] = (
    (JsPath \ "user").write[String] and
    (JsPath \ "action").write[String] and
    (JsPath \ "storyId").write[Long] and
    (JsPath \ "linkId").writeNullable[Long] and
    (JsPath \ "reason").writeNullable[String] and
    (JsPath \ "datetime").write[Long] and
    (JsPath \ "id").write[Long]
  )(unlift(ChangelogJsonModel.unapply))

}
