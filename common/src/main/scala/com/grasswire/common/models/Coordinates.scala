package com.grasswire.common.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Writes, JsPath, Reads }

case class Coordinates(coordinate: Seq[Double], `type`: String)

object Coordinates {
  implicit val CoordinatesReads: Reads[Coordinates] = (
    (JsPath \ "coordinate").read[Seq[Double]] and
    (JsPath \ "type").read[String]
  )(Coordinates.apply _)

  implicit val CoordinateWrites: Writes[Coordinates] = (
    (JsPath \ "coordinate").write[Seq[Double]] and
    (JsPath \ "type").write[String]
  )(unlift(Coordinates.unapply))
}

