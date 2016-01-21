package com.grasswire.common.json_models

import com.grasswire.common.models.Tweet
import play.api.libs.json.Writes

case class UserProfileJsonModel(user: UserJsonModel, storyContributions: List[StoryJsonModel])

object UserProfileJsonModel {
  import play.api.libs.json.{ JsPath, Reads }
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[UserProfileJsonModel] = (
    (JsPath \ "user").read[UserJsonModel] and
    (JsPath \ "storyContributions").read[List[StoryJsonModel]]
  )(UserProfileJsonModel.apply _)

  implicit val writes: Writes[UserProfileJsonModel] = (
    (JsPath \ "user").write[UserJsonModel] and
    (JsPath \ "storyContributions").write[List[StoryJsonModel]]
  )(unlift(UserProfileJsonModel.unapply))
}
