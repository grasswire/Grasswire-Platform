package com.grasswire.common.models

case class SharedSecret(username: String, token: String)

import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads

object SharedSecret {
  implicit val SharedSecretReads: Reads[SharedSecret] = (
    (JsPath \ "username").read[String] and
    (JsPath \ "token").read[String]
  )(SharedSecret.apply _)
}
