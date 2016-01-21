package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class TwitterUser(profile_image_url: String,
  name: String,
  screen_name: String,
  profile_image_url_https: String,
  id: Long)

object TwitterUser {
  implicit val TwitterUserReads: Reads[TwitterUser] = (
    (JsPath \ "profile_image_url").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "screen_name").read[String] and
    (JsPath \ "profile_image_url_https").read[String] and
    (JsPath \ "id").read[Long]
  )(TwitterUser.apply _)

  implicit val TwitterUserWrites: Writes[TwitterUser] = (
    (JsPath \ "profile_image_url").write[String] and
    (JsPath \ "name").write[String] and
    (JsPath \ "screen_name").write[String] and
    (JsPath \ "profile_image_url_https").write[String] and
    (JsPath \ "id").write[Long]
  )(unlift(TwitterUser.unapply))
}
