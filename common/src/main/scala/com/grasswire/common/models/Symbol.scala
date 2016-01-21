package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Symbol(text: String, indices: Seq[Int])

object Symbol {
  implicit val SymbolReads: Reads[Symbol] = (
    (JsPath \ "text").read[String] and
    (JsPath \ "indices").read[Seq[Int]]
  )(Symbol.apply _)

  implicit val SymbolWrites: Writes[Symbol] = (
    (JsPath \ "text").write[String] and
    (JsPath \ "indices").write[Seq[Int]]
  )(unlift(Symbol.unapply))

}

