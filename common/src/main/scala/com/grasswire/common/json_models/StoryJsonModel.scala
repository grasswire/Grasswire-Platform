package com.grasswire.common.json_models

import play.api.libs.json.Writes
import scredis.serialization.Reader
import scredis.serialization.Writer
import play.api.libs.json.Json

/**
 * Created by levinotik on 6/23/15.
 */
case class StoryJsonModel(name: String, headline: Option[String], coverPhoto: Option[String], summary: Option[String], id: Long, createdAt: Long, updatedAt: Long, updatedBy: String, links: List[LinkJsonModel], rank: Int, contributors: List[UserJsonModel], hidden: Boolean)
case class CreateStoryJsonModel(name: String, headline: Option[String], coverPhoto: Option[String], summary: Option[String])
case class EditStoryJsonModel(name: Option[String], headline: Option[String], coverPhoto: Option[String], summary: Option[String], hidden: Option[Boolean])

object EditStoryJsonModel {

  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[EditStoryJsonModel] = (
    (JsPath \ "name").readNullable[String] and
    (JsPath \ "headline").readNullable[String] and
    (JsPath \ "coverPhoto").readNullable[String] and
    (JsPath \ "summary").readNullable[String] and
    (JsPath \ "hidden").readNullable[Boolean]
  )(EditStoryJsonModel.apply _)

  implicit val writes: Writes[EditStoryJsonModel] = (
    (JsPath \ "name").writeNullable[String] and
    (JsPath \ "headline").writeNullable[String] and
    (JsPath \ "coverPhoto").writeNullable[String] and
    (JsPath \ "summary").writeNullable[String] and
    (JsPath \ "hidden").writeNullable[Boolean]
  )(unlift(EditStoryJsonModel.unapply))
}

object StoryJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val redisReader = new scredis.serialization.Reader[StoryJsonModel] {
    def readImpl(bytes: Array[Byte]): StoryJsonModel = Json.parse(bytes).validate[StoryJsonModel].get
  }

  implicit val redisWriter = new Writer[StoryJsonModel] {
    def writeImpl(value: StoryJsonModel): Array[Byte] = Json.toJson(value).toString.getBytes("UTF-8") // new String(Json.toJson(value).toString, "UTF-8")
  }

  implicit val reads: Reads[StoryJsonModel] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "headline").readNullable[String] and
    (JsPath \ "coverPhoto").readNullable[String] and
    (JsPath \ "summary").readNullable[String] and
    (JsPath \ "id").read[Long] and
    (JsPath \ "createdAt").read[Long] and
    (JsPath \ "updatedAt").read[Long] and
    (JsPath \ "updatedBy").read[String] and
    (JsPath \ "links").read[List[LinkJsonModel]] and
    (JsPath \ "rank").read[Int] and
    (JsPath \ "contributors").read[List[UserJsonModel]] and
    (JsPath \ "hidden").read[Boolean]
  )(StoryJsonModel.apply _)

  implicit val writes: Writes[StoryJsonModel] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "headline").writeNullable[String] and
    (JsPath \ "coverPhoto").writeNullable[String] and
    (JsPath \ "summary").writeNullable[String] and
    (JsPath \ "id").write[Long] and
    (JsPath \ "createdAt").write[Long] and
    (JsPath \ "updatedAt").write[Long] and
    (JsPath \ "updatedBy").write[String] and
    (JsPath \ "links").write[List[LinkJsonModel]] and
    (JsPath \ "rank").write[Int] and
    (JsPath \ "contributors").write[List[UserJsonModel]] and
    (JsPath \ "hidden").write[Boolean]
  )(unlift(StoryJsonModel.unapply))

}

object CreateStoryJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[CreateStoryJsonModel] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "headline").readNullable[String] and
    (JsPath \ "coverPhoto").readNullable[String] and
    (JsPath \ "summary").readNullable[String]
  )(CreateStoryJsonModel.apply _)

  implicit val writes: Writes[CreateStoryJsonModel] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "headline").writeNullable[String] and
    (JsPath \ "coverPhoto").writeNullable[String] and
    (JsPath \ "summary").writeNullable[String]
  )(unlift(CreateStoryJsonModel.unapply))

}
