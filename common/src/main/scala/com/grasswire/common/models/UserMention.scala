package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class UserMention(screen_name: String, name: String, id: Long, id_str: String, indices: Seq[Int])

object UserMention {
  implicit val UserMentionReads: Reads[UserMention] = (
    (JsPath \ "screen_name").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "id").read[Long] and
    (JsPath \ "id_str").read[String] and
    (JsPath \ "indices").read[Seq[Int]]
  )(UserMention.apply _)

  implicit val UserMentionWrites: Writes[UserMention] = (
    (JsPath \ "screen_name").write[String] and
    (JsPath \ "name").write[String] and
    (JsPath \ "id").write[Long] and
    (JsPath \ "id_str").write[String] and
    (JsPath \ "indices").write[Seq[Int]]
  )(unlift(UserMention.unapply))
}

