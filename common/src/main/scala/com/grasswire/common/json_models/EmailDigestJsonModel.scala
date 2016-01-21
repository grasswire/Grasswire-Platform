package com.grasswire.common.json_models

case class EmailDigestJsonModel(position: Int, storyId: Long)

object EmailDigestJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[EmailDigestJsonModel] = (
    (JsPath \ "position").read[Int] and
    (JsPath \ "storyId").read[Long]
  )(EmailDigestJsonModel.apply _)
}

case class EmailDigestTemplateModel(position: Int, story: StoryJsonModel)

//object EmailDigestTemplateModel {
//
//  import play.api.libs.json.{ JsPath, Reads }
//  import play.api.libs.functional.syntax._
//
//  implicit val reads: Reads[EmailDigestTemplateModel] = (
//    (JsPath \ "position").read[Int] and
//      (JsPath \ "story").read[StoryJsonModel]
//    )(EmailDigestTemplateModel.apply _)
//}
