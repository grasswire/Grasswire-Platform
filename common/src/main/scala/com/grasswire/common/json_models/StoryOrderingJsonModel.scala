package com.grasswire.common.json_models

import com.grasswire.common.StoryId
import play.api.libs.json.Writes

/**
 * Created by levinotik on 6/25/15.
 */
case class StoryOrderingJsonModel(storyId: StoryId, position: Int)

object StoryOrderingJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[StoryOrderingJsonModel] = (
    (JsPath \ "storyId").read[StoryId] and
    (JsPath \ "position").read[Int]
  )(StoryOrderingJsonModel.apply _)

  implicit val writes: Writes[StoryOrderingJsonModel] = (
    (JsPath \ "storyId").write[StoryId] and
    (JsPath \ "position").write[Int]
  )(unlift(StoryOrderingJsonModel.unapply))

}
