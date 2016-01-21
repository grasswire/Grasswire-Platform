package com.grasswire.common.models

import com.grasswire.common.regex.Regexes
import com.wordnik.swagger.annotations.{ ApiModelProperty, ApiModel }
import play.api.libs.json.{ Writes, JsPath, Reads }

import scala.annotation.meta.field

/**
 * Created by levinotik on 3/16/15.
 */
@ApiModel(description = "A new link submission")
case class LinkSubmission(@(ApiModelProperty @field)(value = "the url for the link submission", dataType = "string", required = true) url: String,
  @(ApiModelProperty @field)(value = "the title for the link submission", dataType = "string", required = true) title: String,
  @(ApiModelProperty @field)(value = "tags to associate with this submission", dataType = "List[String]", required = true) tags: List[String])

sealed trait LinkType

case class TwitterLinkSubmission(tweetId: String) extends LinkType

case object MiscLinkSubmission extends LinkType

object LinkSubmission {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.JsPath
  import play.api.libs.json.Reads

  implicit val jsonReads: Reads[LinkSubmission] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "tags").read[List[String]]
  )(LinkSubmission.apply _)

  implicit val jsonWrites: Writes[LinkSubmission] = (
    (JsPath \ "url").write[String] and
    (JsPath \ "title").write[String] and
    (JsPath \ "tags").write[List[String]]
  )(unlift(LinkSubmission.unapply))

  def getLinkType(linkSubmission: LinkSubmission): LinkType =
    Regexes.twitterStatusId(linkSubmission.url) match {
      case Some(tweetId) => TwitterLinkSubmission(tweetId)
      case _ => MiscLinkSubmission
    }
}
