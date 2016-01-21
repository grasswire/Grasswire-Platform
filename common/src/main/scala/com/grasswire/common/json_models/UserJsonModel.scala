package com.grasswire.common.json_models

import com.grasswire.common.db.GWDatabaseDriver
import com.grasswire.common.db.tables.{ Admins, UserPersistEntity }
import org.joda.time.{ DateTimeZone, DateTime }
import play.api.libs.json.{ Json, Writes }
import scredis.serialization.Reader

/**
 * Created by levinotik on 1/22/15.
 */
case class UserJsonModel(twitterScreenName: String, twitterName: String, email: Option[String], karma: Int, id: Long, createdAt: Long, hellBanned: Boolean, isAdmin: Boolean, profileImageUrl: String)

import play.api.libs.json.{ JsPath, Reads }
import play.api.libs.functional.syntax._
object UserJsonModel {

  implicit val redisReader = new Reader[UserJsonModel] {
    def readImpl(bytes: Array[Byte]): UserJsonModel = Json.parse(bytes).validate[UserJsonModel].get
  }

  def fromPersistEntity(userPersistEntity: UserPersistEntity)(db: GWDatabaseDriver.Backend#DatabaseDef): UserJsonModel = {
    val isAdmin = Admins.isAdmin(userPersistEntity.twitterScreenName)(db).run
    UserJsonModel(userPersistEntity.twitterScreenName, userPersistEntity.twitterName, None, userPersistEntity.karma,
      userPersistEntity.id.get, userPersistEntity.createdAt, userPersistEntity.hellBanned, isAdmin, userPersistEntity.profileImageUrl)
  }

  implicit val reader: Reads[UserJsonModel] = (
    (JsPath \ "twitterScreenName").read[String] and
    (JsPath \ "twitterName").read[String] and
    (JsPath \ "email").readNullable[String] and
    (JsPath \ "karma").read[Int] and
    (JsPath \ "id").read[Long] and
    (JsPath \ "createdAt").read[Long] and
    (JsPath \ "hellBanned").read[Boolean] and
    (JsPath \ "isAdmin").read[Boolean] and
    (JsPath \ "profileImageUrl").read[String]
  )(UserJsonModel.apply _)

  implicit val writer: Writes[UserJsonModel] = (
    (JsPath \ "twitterScreenName").write[String] and
    (JsPath \ "twitterName").write[String] and
    (JsPath \ "email").writeNullable[String] and
    (JsPath \ "karma").write[Int] and
    (JsPath \ "id").write[Long] and
    (JsPath \ "createdAt").write[Long] and
    (JsPath \ "hellBanned").write[Boolean] and
    (JsPath \ "isAdmin").write[Boolean] and
    (JsPath \ "profileImageUrl").write[String]
  )(unlift(UserJsonModel.unapply))
}
