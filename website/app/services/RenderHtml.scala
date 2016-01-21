package services

import api.Api
import com.google.common.net.HttpHeaders
import com.grasswire.common.json_models._
import com.grasswire.common.models._
// import com.grasswire.common.types.PopularSortable
import play.api.Application
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WS
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object RenderHtml {

  private[this] def getAs[A: ClassTag : Reads](url: String, user: Option[String] = None, queryParams: List[(String, String)] = Nil)(implicit ec: ExecutionContext, app: Application): Future[A] = {
    val baseRequest = WS.url(url).withHeaders(HttpHeaders.ACCEPT_ENCODING -> "gzip").withQueryString(queryParams :_*)
    val effectiveRequest = user match {
      case Some(username) => baseRequest.withHeaders("username" -> username)
      case _ => baseRequest
    }

      effectiveRequest.get().map { r =>
        val parsed = Json.parse(r.body).validate[A].get
      parsed
    }
  }
}
