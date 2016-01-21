package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class MediaSize(w: Int, h: Int, resize: String)

object MediaSize {
  implicit val MediaSizeReads: Reads[MediaSize] = (
    (JsPath \ "w").read[Int] and
    (JsPath \ "h").read[Int] and
    (JsPath \ "resize").read[String]
  )(MediaSize.apply _)

  implicit val MediaSizeWrites: Writes[MediaSize] = (
    (JsPath \ "w").write[Int] and
    (JsPath \ "h").write[Int] and
    (JsPath \ "resize").write[String]
  )(unlift(MediaSize.unapply))

}

