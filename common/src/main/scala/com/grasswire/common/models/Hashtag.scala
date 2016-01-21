package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Hashtag(text: String, indices: Seq[Int])

object Hashtag {
  implicit val HashtagReads: Reads[Hashtag] = (
    (JsPath \ "text").read[String] and
    (JsPath \ "indices").read[Seq[Int]]
  )(Hashtag.apply _)

  implicit val HashtagWrites: Writes[Hashtag] = (
    (JsPath \ "text").write[String] and
    (JsPath \ "indices").write[Seq[Int]]
  )(unlift(Hashtag.unapply))

}

