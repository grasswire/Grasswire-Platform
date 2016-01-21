package com.grasswire.common.models

import com.grasswire.common.json_models.UserJsonModel

case class Session(token: String, user: UserJsonModel, isFirstLogin: Boolean)

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._
object Session {
  implicit val SessionReads: Reads[Session] = (
    (JsPath \ "token").read[String] and
    (JsPath \ "user").read[UserJsonModel] and
    (JsPath \ "isFirstLogin").read[Boolean]
  )(Session.apply _)
}