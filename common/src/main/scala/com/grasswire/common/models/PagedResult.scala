package com.grasswire.common.models

case class Paging(offset: Int, limit: Int, total: Int)

case class PagedResult[A](data: List[A], paging: Paging)

object Paging {
  import play.api.libs.functional.syntax._
  import play.api.libs.json.{ JsPath, Reads, Writes }

  implicit val reads: Reads[Paging] = (
    (JsPath \ "offset").read[Int] and
    (JsPath \ "limit").read[Int] and
    (JsPath \ "total").read[Int]
  )(Paging.apply _)

  implicit val writes: Writes[Paging] = (
    (JsPath \ "offset").write[Int] and
    (JsPath \ "limit").write[Int] and
    (JsPath \ "total").write[Int]
  )(unlift(Paging.unapply))
}

object PagedResult {
  import play.api.libs.functional.syntax._
  import play.api.libs.json.{ JsPath, Reads, Writes }

  implicit def reads[A: Reads]: Reads[PagedResult[A]] = (
    (JsPath \ "data").read[List[A]] and
    (JsPath \ "paging").read[Paging]
  )(PagedResult.apply[A] _)

  implicit def writes[A: Writes]: Writes[PagedResult[A]] = (
    (JsPath \ "data").write[List[A]] and
    (JsPath \ "paging").write[Paging]
  )(unlift(PagedResult.unapply[A]))
}
