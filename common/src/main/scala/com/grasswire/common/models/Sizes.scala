package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Sizes(medium: MediaSize, large: MediaSize, thumb: MediaSize, small: MediaSize)

object Sizes {
  implicit val SizesReads: Reads[Sizes] = (
    (JsPath \ "medium").read[MediaSize] and
    (JsPath \ "large").read[MediaSize] and
    (JsPath \ "thumb").read[MediaSize] and
    (JsPath \ "small").read[MediaSize]
  )(Sizes.apply _)

  implicit val SizesWrites: Writes[Sizes] = (
    (JsPath \ "medium").write[MediaSize] and
    (JsPath \ "large").write[MediaSize] and
    (JsPath \ "thumb").write[MediaSize] and
    (JsPath \ "small").write[MediaSize]
  )(unlift(Sizes.unapply))
}

