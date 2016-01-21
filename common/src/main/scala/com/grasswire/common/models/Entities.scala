package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Entities(hashtags: List[Hashtag], symbols: List[Symbol], urls: List[Url], user_mentions: List[UserMention], media: List[Media])

object Entities {
  implicit val EntitiesReads: Reads[Entities] = (
    (JsPath \ "hashtags").read[List[Hashtag]] and
    (JsPath \ "symbols").read[List[Symbol]] and
    (JsPath \ "urls").read[List[Url]] and
    (JsPath \ "user_mentions").read[List[UserMention]] and
    (JsPath \ "media").read[List[Media]]
  )(Entities.apply _)

  implicit val EntitiesWrites: Writes[Entities] = (
    (JsPath \ "hashtags").write[List[Hashtag]] and
    (JsPath \ "symbols").write[List[Symbol]] and
    (JsPath \ "urls").write[List[Url]] and
    (JsPath \ "user_mentions").write[List[UserMention]] and
    (JsPath \ "media").write[List[Media]]
  )(unlift(Entities.unapply))

}

