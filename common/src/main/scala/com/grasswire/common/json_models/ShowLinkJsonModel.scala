package com.grasswire.common.json_models

import play.api.libs.json.Writes

case class ShowLinkJsonModel(link: LinkJsonModel, story: StoryJsonModel)

object ShowLinkJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[ShowLinkJsonModel] = (
    (JsPath \ "link").read[LinkJsonModel] and
    (JsPath \ "story").read[StoryJsonModel]
  )(ShowLinkJsonModel.apply _)

  implicit val writes: Writes[ShowLinkJsonModel] = (
    (JsPath \ "link").write[LinkJsonModel] and
    (JsPath \ "story").write[StoryJsonModel]
  )(unlift(ShowLinkJsonModel.unapply))
}

